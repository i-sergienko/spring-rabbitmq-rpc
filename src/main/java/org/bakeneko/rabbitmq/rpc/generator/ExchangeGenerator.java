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

package org.bakeneko.rabbitmq.rpc.generator;

import java.lang.reflect.Method;

/**
 * RabbitMQ exchange name generator. Used for runtime resolution of exchanges used in
 * {@link org.springframework.amqp.rabbit.core.RabbitTemplate} message sending methods.
 *
 * @author Ivan Sergienko
 */
@FunctionalInterface
public interface ExchangeGenerator {

    /**
     * Generate a RabbitMQ exchange name for the given method and its parameters.
     *
     * @param target the target instance
     * @param method the method being called
     * @param params the method parameters
     * @return a generated exchange name
     */
    String generate(Object target, Method method, Object... params);
}
