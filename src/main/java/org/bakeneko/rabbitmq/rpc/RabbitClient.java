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

import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Annotation to be used on an interface for RabbitMQ RPC client generation.
 * Requires {@link EnableRabbitRPC} annotation to be present on a {@link org.springframework.context.annotation.Configuration} class.
 *
 * @author Ivan Sergienko
 * @see RabbitSender
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RabbitClient {

    /**
     * The name of the {@link Exchange} to used instead of the default one.
     * If also defined in {@link RabbitSender#exchange()}, the latter one takes precedence.
     *
     * @see RabbitSender#exchange()
     */
    String exchange() default "";

    /**
     * The routing key used when sending messages.
     * If also defined in {@link RabbitSender#routingKey()}, the latter one takes precedence.
     *
     * @see RabbitSender#routingKey()
     */
    String routingKey() default "";

    /**
     * The bean name of the custom {@link MessagePostProcessor}
     * to use.
     * If also defined in {@link RabbitSender#messagePostProcessor()}, the latter one takes precedence.
     *
     * @see RabbitSender#messagePostProcessor()
     */
    String messagePostProcessor() default "";

}
