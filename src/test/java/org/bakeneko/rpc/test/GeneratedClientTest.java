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

package org.bakeneko.rpc.test;

import org.bakeneko.rpc.test.model.TestRequest;
import org.bakeneko.rpc.test.model.TestResponse;
import org.bakeneko.rpc.test.service.GeneratorTestingClient;
import org.bakeneko.rpc.test.service.TestClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.bakeneko.rpc.test.TestConfiguration.TEST_HEADER_VALUE;
import static org.junit.Assert.assertEquals;

/**
 * @author Ivan Sergienko
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class)
public class GeneratedClientTest {
    @Autowired
    private TestClient client;
    @Autowired
    private GeneratorTestingClient generatorTestingClient;

    @Test
    public void payloadOnlyListenerReturnsRequestValue() {
        String response = client.sendAndReceiveString("test");
        assertEquals("test", response);
    }

    @Test
    public void payloadAndHeaderListenerReturnsRequestValue() {
        String response = client.sendWithHeaderAndReceiveString("test", TEST_HEADER_VALUE);
        assertEquals("test", response);
    }

    @Test
    public void generatedClientUsesSpecifiedMessagePostProcessor() {
        String response = client.sendWithMessagePostProcessorAndReceiveString("test");
        assertEquals("test", response);
    }

    @Test
    public void sendMessageAsynchronously() {
        client.sendAsynchronously("test");
    }

    @Test
    public void sendAndReceiveCustomModel() {
        TestResponse response = client.sendAndReceiveCustomModel(new TestRequest("test"));
        assertEquals(new TestResponse("test"), response);
    }

    @Test
    public void sendWithHeaderAndReceiveCustomModel() {
        TestResponse response = client.sendWithHeaderAndReceiveCustomModel(new TestRequest("test"), TEST_HEADER_VALUE);
        assertEquals(new TestResponse("test"), response);
    }

    @Test
    public void sendWithGeneratedRoutingKey() {
        String response = generatorTestingClient.sendWithGeneratedRoutingKey("test");
        assertEquals("test", response);
    }

    @Test
    public void sendWithDefaultGeneratedRoutingKey() {
        String response = generatorTestingClient.sendWithDefaultGeneratedRoutingKey("test");
        assertEquals("test", response);
    }
}
