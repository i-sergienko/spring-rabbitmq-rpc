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

/**
 * A factory for Rabbit RPC client generation.
 *
 * @author Ivan Sergienko
 * @see RabbitClient
 * @see RabbitSender
 */
public interface RabbitClientFactory {

    /**
     * Generates a Rabbit RPC client from the {@code toImplement} interface.
     *
     * @param toImplement the interface to generate an RPC client implementation from.
     * @param <T>         type of the interface.
     * @return the implementation of the {@code toImplement} interface.
     */
    <T> T forType(Class<T> toImplement);

    /**
     * Generates a Rabbit RPC client from the {@code toImplement} interface.
     *
     * @param classLoader the {@link ClassLoader} to be used for {@link java.lang.reflect.Proxy} generation.
     * @param toImplement the interface to generate an RPC client implementation from.
     * @param <T>         type of the interface.
     * @return the implementation of the {@code toImplement} interface.
     */
    <T> T forType(ClassLoader classLoader, Class<T> toImplement);
}
