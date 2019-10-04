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

import java.util.Map;

/**
 * Annotation processor for {@link RabbitClient} generation.
 *
 * @author Ivan Sergienko
 */
public interface RabbitClientAnnotationProcessor {

    /**
     * Reads the metadata necessary for Rabbit RPC client generation from {@link RabbitClient}  and {@link RabbitSender}  annotations.
     *
     * @param toImplement the interface annotated with {@link RabbitClient}
     * @return a {@code Map<String, RabbitClientMetadata>} containing metadata for each method in {@code toImplement} interface
     */
    Map<String, RabbitClientMetadata> readMetadata(Class<?> toImplement);
}
