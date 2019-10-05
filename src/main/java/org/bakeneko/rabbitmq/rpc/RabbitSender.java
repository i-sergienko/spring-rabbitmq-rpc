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

package org.bakeneko.rabbitmq.rpc;

import java.lang.annotation.*;

/**
 * An annotation used on methods in interfaces marked with {@link RabbitClient} to override the parameters specified in {@link RabbitClient} for a particular method.
 * I.e. the {@link RabbitClient} is used to specify defaults for all methods in the interface, and the {@link RabbitSender} is used to fine-tune the metadata for a particular method.
 *
 * @author Ivan Sergienko
 * @see RabbitClient
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RabbitSender {
    /**
     * Specifies the exchange for {@link org.springframework.amqp.rabbit.core.RabbitTemplate} to send the messages to.
     * If not specified, the default exchange is used.
     *
     * @return the exchange to use while sending messages through RabbitMQ. Overrides the one defined in {@link RabbitClient#exchange()} (if any)
     * @see RabbitClient#exchange()
     */
    String exchange() default "";

    /**
     * The bean name of the custom {@link org.bakeneko.rabbitmq.rpc.generator.ExchangeGenerator} to use.
     * <p>Mutually exclusive with the {@link #exchange} attribute.
     *
     * @see RabbitClient#exchangeGenerator
     */
    String exchangeGenerator() default "";

    /**
     * Specifies the routing key for {@link org.springframework.amqp.rabbit.core.RabbitTemplate} to send the messages to.
     * If not specified, the default routing key is used.
     *
     * @return the routing key to use while sending messages through RabbitMQ. Overrides the one defined in {@link RabbitClient#routingKey()} (if any)
     * @see RabbitClient#routingKey()
     */
    String routingKey() default "";

    /**
     * The bean name of the custom {@link org.bakeneko.rabbitmq.rpc.generator.RoutingKeyGenerator} to use.
     * <p>Mutually exclusive with the {@link #routingKey} attribute.
     *
     * @see RabbitClient#routingKeyGenerator
     */
    String routingKeyGenerator() default "";

    /**
     * The bean name of the custom {@link org.springframework.amqp.core.MessagePostProcessor}
     * to use.
     *
     * @return the bean name of a {@link org.springframework.amqp.core.MessagePostProcessor} to use before sending messages. Overrides the one defined in {@link RabbitClient#messagePostProcessor()} (if any)
     * @see RabbitClient#messagePostProcessor()
     */
    String messagePostProcessor() default "";

}
