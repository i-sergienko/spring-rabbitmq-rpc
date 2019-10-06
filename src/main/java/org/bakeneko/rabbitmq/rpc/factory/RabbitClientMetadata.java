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
import org.bakeneko.rabbitmq.rpc.generator.ExchangeGenerator;
import org.bakeneko.rabbitmq.rpc.generator.RoutingKeyGenerator;
import org.springframework.amqp.core.MessagePostProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Method metadata container for {@link RabbitClient} generation.
 * Contains data necessary for message dispatching through {@link org.springframework.amqp.rabbit.core.RabbitTemplate}.
 *
 * @author Ivan Sergienko
 */
public class RabbitClientMetadata {
    private final ExchangeGenerator exchangeGenerator;
    private final RoutingKeyGenerator routingKeyGenerator;
    private final MessagePostProcessor messagePostProcessor;
    private final Integer payloadParameterIndex;
    private final Integer headerMapParameterIndex;
    private final Map<String, Integer> headerParameterIndexByName;

    public RabbitClientMetadata(
            ExchangeGenerator exchangeGenerator,
            RoutingKeyGenerator routingKeyGenerator,
            MessagePostProcessor messagePostProcessor,
            Integer payloadParameterIndex,
            Integer headerMapParameterIndex,
            Map<String, Integer> headerParameterIndexByName
    ) {
        this.exchangeGenerator = exchangeGenerator != null ? exchangeGenerator : (target, method, params) -> null;
        this.routingKeyGenerator = routingKeyGenerator != null ? routingKeyGenerator : (target, method, params) -> null;
        this.messagePostProcessor = messagePostProcessor != null ? messagePostProcessor : message -> message;
        this.payloadParameterIndex = payloadParameterIndex;
        this.headerMapParameterIndex = headerMapParameterIndex;
        this.headerParameterIndexByName = headerParameterIndexByName;
    }

    public ExchangeGenerator getExchangeGenerator() {
        return exchangeGenerator;
    }

    public RoutingKeyGenerator getRoutingKeyGenerator() {
        return routingKeyGenerator;
    }

    public MessagePostProcessor getMessagePostProcessor() {
        return messagePostProcessor;
    }

    public Object getPayload(Object[] args) {
        return payloadParameterIndex != null ? args[payloadParameterIndex] : null;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getHeaders(Object[] args) {
        Map<String, Object> allHeaders = new HashMap<>();
        if (headerMapParameterIndex != null) {
            Map<String, Object> headerMap = (Map<String, Object>) args[headerMapParameterIndex];
            allHeaders.putAll(headerMap);
        }
        Map<String, Object> singleHeaders = headerParameterIndexByName.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> args[entry.getValue()]));
        allHeaders.putAll(singleHeaders);

        return allHeaders;
    }
}
