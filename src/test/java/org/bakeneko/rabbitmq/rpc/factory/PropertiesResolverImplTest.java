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

import i.sergienko.rpc.test.TestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author Ivan Sergienko
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class)
public class PropertiesResolverImplTest {
    @Autowired
    private PropertiesResolver propertiesResolver;

    @Test
    public void shouldReturnValueIfNotReference() {
        String value = "not_a_reference";
        String resolved = propertiesResolver.replaceIfProperty(value);

        assertEquals(value, resolved);
    }

    @Test
    public void shouldReturnPropertyIfReference() {
        String reference = "${rpc-test.queue.payload-and-header}";
        String resolved = propertiesResolver.replaceIfProperty(reference);

        assertEquals("payload_and_header", resolved);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfMissingReference() {
        String reference = "${missing.property}";
        propertiesResolver.replaceIfProperty(reference);
    }
}