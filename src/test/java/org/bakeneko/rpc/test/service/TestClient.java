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

package org.bakeneko.rpc.test.service;

import org.bakeneko.rabbitmq.rpc.RabbitClient;
import org.bakeneko.rabbitmq.rpc.RabbitSender;
import org.bakeneko.rpc.test.model.TestRequest;
import org.bakeneko.rpc.test.model.TestResponse;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import static org.bakeneko.rpc.test.TestConfiguration.TEST_HEADER_NAME;

/**
 * @author Ivan Sergienko
 */
@RabbitClient
public interface TestClient {

    @RabbitSender(routingKey = "${rpc-test.queue.payload-only}")
    String sendAndReceiveString(String payload);

    @RabbitSender(routingKey = "${rpc-test.queue.payload-and-header}")
    String sendWithHeaderAndReceiveString(@Payload String payload, @Header(TEST_HEADER_NAME) String header);

    @RabbitSender(routingKey = "${rpc-test.queue.payload-and-header}", messagePostProcessor = "testHeaderAppendingPostProcessor")
    String sendWithMessagePostProcessorAndReceiveString(@Payload String payload);

    @RabbitSender(routingKey = "${rpc-test.queue.asynchronous}")
    void sendAsynchronously(String payload);

    @RabbitSender(routingKey = "${rpc-test.queue.custom-payload-only}")
    TestResponse sendAndReceiveCustomModel(TestRequest request);

    @RabbitSender(routingKey = "${rpc-test.queue.custom-payload-and-header}")
    TestResponse sendWithHeaderAndReceiveCustomModel(@Payload TestRequest request, @Header(TEST_HEADER_NAME) String header);

}
