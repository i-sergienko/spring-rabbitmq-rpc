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
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Map;

import static org.bakeneko.rabbitmq.rpc.factory.ReflectionUtils.methodNameSignatureAware;

/**
 * @author Ivan Sergienko
 */
public class RabbitClientFactoryImpl implements RabbitClientFactory {
    private RabbitClientAnnotationProcessor annotationProcessor;
    private RabbitTemplate rabbitTemplate;

    public RabbitClientFactoryImpl(
            RabbitTemplate rabbitTemplate,
            RabbitClientAnnotationProcessor annotationProcessor
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.annotationProcessor = annotationProcessor;
    }

    public <T> T forType(Class<T> toImplement) {
        return forType(RabbitClientFactoryImpl.class.getClassLoader(), toImplement);
    }

    @SuppressWarnings("unchecked")
    public <T> T forType(ClassLoader classLoader, Class<T> toImplement) {
        if (toImplement.isAnnotationPresent(RabbitClient.class)) {
            Map<String, RabbitClientMetadata> metadata = annotationProcessor.readMetadata(toImplement);

            return (T) Proxy.newProxyInstance(
                    classLoader,
                    new Class[]{toImplement},
                    new ObjectMethodsDelegatingInvocationHandler(invocationHandler(metadata))
            );
        } else {
            throw new IllegalArgumentException("The class provided is not a @RabbitClient: " + toImplement.getCanonicalName());
        }
    }

    private InvocationHandler invocationHandler(Map<String, RabbitClientMetadata> metadataByMethod) {
        return (proxy, method, args) -> {
            RabbitClientMetadata metadata = metadataByMethod.get(methodNameSignatureAware(method));
            String exchange = metadata.getExchangeGenerator().generate(proxy, method, args);
            String routingKey = metadata.getRoutingKeyGenerator().generate(proxy, method, args);
            MessagePostProcessor postProcessor = headerAppendingPostProcessorWrapper(metadata.getHeaders(args), metadata.getMessagePostProcessor());

            if ("void".equals(method.getAnnotatedReturnType().getType().getTypeName())) {
                sendAsync(exchange, routingKey, metadata.getPayload(args), postProcessor);

                return null;
            } else {
                return sendAndReceive(exchange, routingKey, metadata.getPayload(args), method.getGenericReturnType(), postProcessor);
            }
        };
    }

    private MessagePostProcessor headerAppendingPostProcessorWrapper(Map<String, Object> headers, MessagePostProcessor postProcessor) {
        return message -> {
            headers.forEach((name, value) -> message.getMessageProperties().getHeaders().put(name, value));

            return postProcessor.postProcessMessage(message);
        };
    }

    private <V> V sendAndReceive(
            String exchange,
            String routingKey,
            Object payload,
            Type returnType,
            MessagePostProcessor postProcessor
    ) {
        ParameterizedTypeReference<V> returnTypeReference = ParameterizedTypeReference.forType(returnType);

        if (exchange != null) {
            return rabbitTemplate.convertSendAndReceiveAsType(exchange, routingKey, payload, postProcessor, returnTypeReference);
        } else if (routingKey != null) {
            return rabbitTemplate.convertSendAndReceiveAsType(routingKey, payload, postProcessor, returnTypeReference);
        } else {
            return rabbitTemplate.convertSendAndReceiveAsType(payload, postProcessor, returnTypeReference);
        }
    }

    private void sendAsync(
            String exchange,
            String routingKey,
            Object payload,
            MessagePostProcessor postProcessor
    ) {
        if (exchange != null) {
            rabbitTemplate.convertAndSend(exchange, routingKey, payload, postProcessor);
        } else if (routingKey != null) {
            rabbitTemplate.convertAndSend(routingKey, payload, postProcessor);
        } else {
            rabbitTemplate.convertAndSend(payload, postProcessor);
        }
    }
}
