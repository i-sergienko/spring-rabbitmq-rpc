/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bakeneko.rabbitmq.rpc.factory;

import org.bakeneko.rabbitmq.rpc.RabbitClient;
import org.bakeneko.rabbitmq.rpc.RabbitSender;
import org.bakeneko.rpc.test.model.TestRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.doReturn;

/**
 * @author Ivan Sergienko
 */
@RunWith(MockitoJUnitRunner.class)
public class RabbitClientAnnotationProcessorImplTest {
    private static final String IDENTITY_POST_PROCESSOR_BEAN_NAME = "somePostProcessor";
    private static final MessagePostProcessor IDENTITY_MESSAGE_POST_PROCESSOR = message -> message;
    private static final String ANOTHER_POST_PROCESSOR_BEAN_NAME = "anotherPostProcessor";
    private static final MessagePostProcessor ANOTHER_MESSAGE_POST_PROCESSOR = message -> message;

    private static final String DEFAULT_EXCHANGE = "default_exchange";
    private static final String DEFAULT_QUEUE = "default_queue";
    private static final String METHOD_LEVEL_EXCHANGE = "test_exchange";
    private static final String METHOD_LEVEL_QUEUE = "test_queue";

    private RabbitClientAnnotationProcessor annotationProcessor;

    @Mock
    private ContextSupport contextSupport;

    @Before
    public void init() {
        doReturn(IDENTITY_MESSAGE_POST_PROCESSOR).when(contextSupport).getBean(IDENTITY_POST_PROCESSOR_BEAN_NAME, MessagePostProcessor.class);
        doReturn(ANOTHER_MESSAGE_POST_PROCESSOR).when(contextSupport).getBean(ANOTHER_POST_PROCESSOR_BEAN_NAME, MessagePostProcessor.class);
        annotationProcessor = new RabbitClientAnnotationProcessorImpl(value -> value, contextSupport);
    }

    @Test
    public void readMetadata_correct_client() throws NoSuchMethodException {
        Map<String, RabbitClientMetadata> metadataByMethod = annotationProcessor.readMetadata(CorrectClient.class);

        String sendAndReceive = ReflectionUtils.methodNameSignatureAware(CorrectClient.class.getMethod("sendAndReceive", String.class));
        RabbitClientMetadata sendAndReceiveMetadata = metadataByMethod.get(sendAndReceive);
        assertEquals(METHOD_LEVEL_EXCHANGE, sendAndReceiveMetadata.getExchangeGenerator().generate(null, null, null));
        assertEquals(METHOD_LEVEL_QUEUE, sendAndReceiveMetadata.getRoutingKeyGenerator().generate(null, null, null));
        assertEquals(IDENTITY_MESSAGE_POST_PROCESSOR, sendAndReceiveMetadata.getMessagePostProcessor());
        assertEquals("test", sendAndReceiveMetadata.getPayload(new Object[]{"test"}));

        String sendAndReceivePayloadAnnotation = ReflectionUtils.methodNameSignatureAware(CorrectClient.class.getMethod("sendAndReceivePayloadAnnotation", String.class));
        RabbitClientMetadata sendAndReceivePayloadAnnotationMetadata = metadataByMethod.get(sendAndReceivePayloadAnnotation);
        assertEquals(DEFAULT_EXCHANGE, sendAndReceivePayloadAnnotationMetadata.getExchangeGenerator().generate(null, null, null));
        assertEquals(METHOD_LEVEL_QUEUE, sendAndReceivePayloadAnnotationMetadata.getRoutingKeyGenerator().generate(null, null, null));
        assertNotEquals(IDENTITY_MESSAGE_POST_PROCESSOR, sendAndReceivePayloadAnnotationMetadata.getMessagePostProcessor());
        assertEquals("test", sendAndReceivePayloadAnnotationMetadata.getPayload(new Object[]{"test"}));

        String sendAndReceiveWithHeader = ReflectionUtils.methodNameSignatureAware(CorrectClient.class.getMethod("sendAndReceiveWithHeader", String.class, String.class));
        RabbitClientMetadata sendAndReceiveWithHeaderMetadata = metadataByMethod.get(sendAndReceiveWithHeader);
        assertEquals(METHOD_LEVEL_EXCHANGE, sendAndReceiveWithHeaderMetadata.getExchangeGenerator().generate(null, null, null));
        assertEquals(DEFAULT_QUEUE, sendAndReceiveWithHeaderMetadata.getRoutingKeyGenerator().generate(null, null, null));
        assertNotEquals(IDENTITY_MESSAGE_POST_PROCESSOR, sendAndReceivePayloadAnnotationMetadata.getMessagePostProcessor());
        assertEquals("payload", sendAndReceiveWithHeaderMetadata.getPayload(new Object[]{"header", "payload"}));
        Map<String, Object> headers = sendAndReceiveWithHeaderMetadata.getHeaders(new Object[]{"header", "payload"});
        assertEquals("header", headers.get("someHeader"));

        String sendAsynch = ReflectionUtils.methodNameSignatureAware(CorrectClient.class.getMethod("sendAsynch", TestRequest.class));
        RabbitClientMetadata sendAsynchMetadata = metadataByMethod.get(sendAsynch);
        assertEquals(DEFAULT_EXCHANGE, sendAsynchMetadata.getExchangeGenerator().generate(null, null, null));
        assertEquals(DEFAULT_QUEUE, sendAsynchMetadata.getRoutingKeyGenerator().generate(null, null, null));
        assertNotEquals(IDENTITY_MESSAGE_POST_PROCESSOR, sendAndReceivePayloadAnnotationMetadata.getMessagePostProcessor());
        TestRequest request = new TestRequest("test");
        assertEquals(request, sendAsynchMetadata.getPayload(new Object[]{request}));
    }

    @Test
    public void readMetadata_default_post_processor_on_interface() throws NoSuchMethodException {
        Map<String, RabbitClientMetadata> metadataByMethod = annotationProcessor.readMetadata(MethodPostProcessorOverridesDefault.class);

        String defaultProcessorMethod = ReflectionUtils.methodNameSignatureAware(MethodPostProcessorOverridesDefault.class.getMethod("defaultProcessor", String.class));
        RabbitClientMetadata defaultProcessorMetadata = metadataByMethod.get(defaultProcessorMethod);
        assertNotEquals(ANOTHER_MESSAGE_POST_PROCESSOR, defaultProcessorMetadata.getMessagePostProcessor());
        assertEquals(IDENTITY_MESSAGE_POST_PROCESSOR, defaultProcessorMetadata.getMessagePostProcessor());

        String overriddenProcessorMethod = ReflectionUtils.methodNameSignatureAware(MethodPostProcessorOverridesDefault.class.getMethod("overrideProcessor", String.class));
        RabbitClientMetadata overriddenProcessorMetadata = metadataByMethod.get(overriddenProcessorMethod);
        assertNotEquals(IDENTITY_MESSAGE_POST_PROCESSOR, overriddenProcessorMetadata.getMessagePostProcessor());
        assertEquals(ANOTHER_MESSAGE_POST_PROCESSOR, overriddenProcessorMetadata.getMessagePostProcessor());
    }

    @Test
    public void readMetadata_header_map_parameter() {
        RabbitClientMetadata metadata = annotationProcessor.readMetadata(HeaderMapClient.class).values().stream().findFirst().get();
        Map<String, String> headers = Collections.singletonMap("some_header", "some_value");
        String payload = "some payload";
        Object[] args = new Object[]{payload, headers};

        assertEquals(payload, metadata.getPayload(args));
        assertEquals(headers, metadata.getHeaders(args));
    }

    @Test(expected = IllegalStateException.class)
    public void readMetadata_ambiguous_method_parameters() {
        annotationProcessor.readMetadata(MissingPayloadAnnotationClient.class);
    }

    @Test(expected = IllegalStateException.class)
    public void readMetadata_only_header_parameter() {
        annotationProcessor.readMetadata(HeaderOnlyMethodClient.class);
    }

    @Test(expected = IllegalStateException.class)
    public void readMetadata_missing_payload() {
        annotationProcessor.readMetadata(MissingPayloadClient.class);
    }

    @RabbitClient(exchange = DEFAULT_EXCHANGE, routingKey = DEFAULT_QUEUE)
    interface CorrectClient {
        @RabbitSender(routingKey = METHOD_LEVEL_QUEUE, exchange = METHOD_LEVEL_EXCHANGE, messagePostProcessor = IDENTITY_POST_PROCESSOR_BEAN_NAME)
        String sendAndReceive(String payload);

        @RabbitSender(routingKey = METHOD_LEVEL_QUEUE)
        String sendAndReceivePayloadAnnotation(@Payload String payload);

        @RabbitSender(exchange = METHOD_LEVEL_EXCHANGE)
        String sendAndReceiveWithHeader(@Header String someHeader, @Payload String payload);

        void sendAsynch(@Payload TestRequest request);
    }

    @RabbitClient(messagePostProcessor = IDENTITY_POST_PROCESSOR_BEAN_NAME)
    interface MethodPostProcessorOverridesDefault {
        @RabbitSender
        String defaultProcessor(String payload);

        @RabbitSender(messagePostProcessor = ANOTHER_POST_PROCESSOR_BEAN_NAME)
        String overrideProcessor(String payload);
    }

    @RabbitClient
    interface MissingPayloadAnnotationClient {
        @RabbitSender
        String ambiguousParameters(String somePayload, @Header String someHeader);
    }

    @RabbitClient
    interface HeaderOnlyMethodClient {
        String onlyHeader(@Header String someHeader);
    }

    @RabbitClient
    interface MissingPayloadClient {
        String noPayload();
    }

    @RabbitClient
    interface HeaderMapClient {
        String specifyHeaderMap(@Payload String payload, @Headers Map<String, String> someHeaders);
    }
}