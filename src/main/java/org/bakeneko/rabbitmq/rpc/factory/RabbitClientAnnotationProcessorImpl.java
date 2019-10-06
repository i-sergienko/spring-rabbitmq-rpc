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
import org.bakeneko.rabbitmq.rpc.generator.ExchangeGenerator;
import org.bakeneko.rabbitmq.rpc.generator.RoutingKeyGenerator;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * @author Ivan Sergienko
 */
public class RabbitClientAnnotationProcessorImpl implements RabbitClientAnnotationProcessor {
    private PropertiesResolver propertiesResolver;
    private ContextSupport contextSupport;

    public RabbitClientAnnotationProcessorImpl(
            PropertiesResolver propertiesResolver,
            ContextSupport contextSupport
    ) {
        this.propertiesResolver = propertiesResolver;
        this.contextSupport = contextSupport;
    }

    @Override
    public Map<String, RabbitClientMetadata> readMetadata(Class<?> toImplement) {
        if (toImplement.isAnnotationPresent(RabbitClient.class)) {
            RabbitClient rabbitClient = toImplement.getAnnotation(RabbitClient.class);
            ExchangeGenerator defaultExchangeGenerator = getExchangeGenerator(rabbitClient);
            RoutingKeyGenerator defaultRoutingKeyGenerator = getRoutingKeyGenerator(rabbitClient);
            MessagePostProcessor defaultMessagePostProcessor = getMessagePostProcessor(rabbitClient.messagePostProcessor());

            Map<String, Method> methodsByName = Stream.of(toImplement.getDeclaredMethods())
                    .collect(toMap(ReflectionUtils::methodNameSignatureAware, m -> m));

            return methodsByName.entrySet()
                    .stream()
                    .collect(toMap(Map.Entry::getKey, entry -> {
                        Method method = entry.getValue();
                        Integer payloadParameterIndex = getPayloadParameterIndex(method);
                        Integer headerMapParameterIndex = getHeaderMapParameterIndex(method);
                        Map<String, Integer> headerParameterIndexByName = getHeaderParameterIndexByName(method);

                        if (method.isAnnotationPresent(RabbitSender.class)) {
                            RabbitSender rabbitSender = method.getAnnotation(RabbitSender.class);

                            ExchangeGenerator exchangeGenerator = getExchangeGenerator(rabbitSender);
                            RoutingKeyGenerator routingKeyGenerator = getRoutingKeyGenerator(rabbitSender);
                            MessagePostProcessor messagePostProcessor = getMessagePostProcessor(rabbitSender.messagePostProcessor());

                            return new RabbitClientMetadata(
                                    exchangeGenerator != null ? exchangeGenerator : defaultExchangeGenerator,
                                    routingKeyGenerator != null ? routingKeyGenerator : defaultRoutingKeyGenerator,
                                    messagePostProcessor != null ? messagePostProcessor : defaultMessagePostProcessor,
                                    payloadParameterIndex,
                                    headerMapParameterIndex,
                                    headerParameterIndexByName
                            );
                        } else {
                            return new RabbitClientMetadata(
                                    defaultExchangeGenerator,
                                    defaultRoutingKeyGenerator,
                                    defaultMessagePostProcessor,
                                    payloadParameterIndex,
                                    headerMapParameterIndex,
                                    headerParameterIndexByName
                            );
                        }
                    }));
        } else {
            throw new IllegalArgumentException("The class provided is not a @RabbitClient: " + toImplement.getCanonicalName());
        }
    }

    private Integer getPayloadParameterIndex(Method method) {
        if (method.getParameterCount() == 1 && !isHeader(method.getParameters()[0])) {
            return 0;
        } else if (method.getParameterCount() == 0 || (method.getParameterCount() == 1 && isHeader(method.getParameters()[0]))) {
            throw new IllegalStateException(String.format("No @Payload parameters found in %s.%s. " +
                            "At least one @Payload parameter must be present - if a parameter is the only one and not marked as @Header, it's also considered @Payload.",
                    method.getDeclaringClass().getName(), method.getName()));
        } else {
            for (int i = 0; i < method.getParameterCount(); i++) {
                if (isPayload(method.getParameters()[i])) {
                    return i;
                }
            }

            throw new IllegalStateException(String.format("Multiple parameters specified in %s.%s, but none of them is marked as @Payload.",
                    method.getDeclaringClass().getName(), method.getName()));
        }
    }

    private Map<String, Integer> getHeaderParameterIndexByName(Method method) {
        Map<String, Integer> indexByName = new HashMap<>();

        for (AtomicInteger i = new AtomicInteger(0); i.get() < method.getParameterCount(); i.incrementAndGet()) {
            Stream.of(method.getParameterAnnotations()[i.get()]).filter(it -> it.annotationType().equals(Header.class))
                    .findFirst()
                    .ifPresent(annotation -> {
                        Header header = (Header) annotation;
                        String headerName;
                        if (!header.value().isEmpty()) {
                            headerName = header.value();
                        } else if (!header.name().isEmpty()) {
                            headerName = header.name();
                        } else {
                            headerName = method.getParameters()[i.get()].getName();
                        }

                        indexByName.put(headerName, i.get());
                    });
        }

        return indexByName;
    }

    private Integer getHeaderMapParameterIndex(Method method) {
        long headerMapParameters = Stream.of(method.getParameters()).filter(this::isHeaderMap).count();

        if (headerMapParameters == 1) {
            for (int i = 0; i < method.getParameterCount(); i++) {
                if (isHeaderMap(method.getParameters()[i])) {
                    Class<?> parameterType = method.getParameters()[i].getType();
                    if (Map.class.isAssignableFrom(parameterType)) {
                        return i;
                    } else {
                        throw new IllegalArgumentException(
                                String.format("Parameters marked as @Headers must be assignable to java.util.Map<String, Object>, while %s.%s has type %s.",
                                        method.getDeclaringClass().getName(), method.getName(), parameterType)
                        );
                    }
                }
            }
        } else if (headerMapParameters > 1) {
            throw new IllegalStateException(String.format("Multiple @Headers parameters specified in %s.%s, while a maximum of 1 is allowed.",
                    method.getDeclaringClass().getName(), method.getName()));
        }

        return null;
    }

    private boolean isHeader(Parameter parameter) {
        return hasAnnotation(parameter, Header.class);
    }

    private boolean isHeaderMap(Parameter parameter) {
        return hasAnnotation(parameter, Headers.class);
    }

    private boolean isPayload(Parameter parameter) {
        return hasAnnotation(parameter, Payload.class);
    }

    private <T extends Annotation> boolean hasAnnotation(Parameter parameter, Class<T> annotationType) {
        return Stream.of(parameter.getAnnotations()).anyMatch(it -> it.annotationType().equals(annotationType));
    }

    private MessagePostProcessor getMessagePostProcessor(String beanName) {
        if (!beanName.isEmpty()) {
            return getRequiredBean(beanName, MessagePostProcessor.class);
        } else {
            return null;
        }
    }

    private ExchangeGenerator getExchangeGenerator(RabbitClient rabbitClient) {
        return getExchangeGenerator(rabbitClient.exchange(), rabbitClient.exchangeGenerator());
    }

    private ExchangeGenerator getExchangeGenerator(RabbitSender rabbitSender) {
        return getExchangeGenerator(rabbitSender.exchange(), rabbitSender.exchangeGenerator());
    }

    private ExchangeGenerator getExchangeGenerator(String exchange, String exchangeGenerator) {
        if (!exchange.isEmpty() && !exchangeGenerator.isEmpty()) {
            throw new IllegalStateException("Both 'exchange' and 'exchangeGenerator' specified in @RabbitClient, although they are mutually exclusive.");
        } else if (!exchange.isEmpty()) {
            String hardCodedExchange = propertiesResolver.replaceIfProperty(exchange);

            return (target, method, params) -> hardCodedExchange;
        } else if (!exchangeGenerator.isEmpty()) {
            return getRequiredBean(exchangeGenerator, ExchangeGenerator.class);
        } else {
            return null;
        }
    }

    private RoutingKeyGenerator getRoutingKeyGenerator(RabbitClient rabbitClient) {
        return getRoutingKeyGenerator(rabbitClient.routingKey(), rabbitClient.routingKeyGenerator());
    }

    private RoutingKeyGenerator getRoutingKeyGenerator(RabbitSender rabbitSender) {
        return getRoutingKeyGenerator(rabbitSender.routingKey(), rabbitSender.routingKeyGenerator());
    }

    private RoutingKeyGenerator getRoutingKeyGenerator(String routingKey, String routingKeyGenerator) {
        if (!routingKey.isEmpty() && !routingKeyGenerator.isEmpty()) {
            throw new IllegalStateException("Both 'routingKey' and 'routingKeyGenerator' specified in @RabbitClient, although they are mutually exclusive.");
        } else if (!routingKey.isEmpty()) {
            String hardCodedRoutingKey = propertiesResolver.replaceIfProperty(routingKey);

            return (target, method, params) -> hardCodedRoutingKey;
        } else if (!routingKeyGenerator.isEmpty()) {
            return getRequiredBean(routingKeyGenerator, RoutingKeyGenerator.class);
        } else {
            return null;
        }
    }

    private <T> T getRequiredBean(String beanName, Class<T> beanType) {
        try {
            return contextSupport.getBean(beanName, beanType);
        } catch (Exception e) {
            throw new IllegalStateException("Required bean with name \"" + beanName + "\" could not be found, although it is required by a @RabbitClient", e);
        }
    }
}
