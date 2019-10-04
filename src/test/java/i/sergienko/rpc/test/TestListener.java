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

package i.sergienko.rpc.test;

import i.sergienko.rpc.test.model.TestRequest;
import i.sergienko.rpc.test.model.TestResponse;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import static i.sergienko.rpc.test.TestConfiguration.TEST_HEADER_NAME;
import static i.sergienko.rpc.test.TestConfiguration.TEST_HEADER_VALUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Ivan Sergienko
 */
@Service
public class TestListener {
    @RabbitListener(queues = "${rpc-test.queue.payload-only}")
    public String primitivePayload(@Payload String payload) {
        return payload;
    }

    @RabbitListener(queues = "${rpc-test.queue.payload-and-header}")
    public String primitivePayloadAndHeader(@Payload String payload, @Header(TEST_HEADER_NAME) String header) {
        assertEquals(TEST_HEADER_VALUE, header);
        return payload;
    }

    @RabbitListener(queues = "${rpc-test.queue.asynchronous}")
    public void noreply(@Payload String payload) {
        assertNotNull(payload);
    }

    @RabbitListener(queues = "${rpc-test.queue.custom-payload-only}")
    public TestResponse customModels(@Payload TestRequest payload) {
        return new TestResponse(payload.getData());
    }

    @RabbitListener(queues = "${rpc-test.queue.custom-payload-and-header}")
    public TestResponse customPayloadWithHeader(@Payload TestRequest payload, @Header(TEST_HEADER_NAME) String header) {
        assertEquals(TEST_HEADER_VALUE, header);
        return new TestResponse(payload.getData());
    }
}
