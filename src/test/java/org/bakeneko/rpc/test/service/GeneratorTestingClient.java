package org.bakeneko.rpc.test.service;

import org.bakeneko.rabbitmq.rpc.RabbitClient;
import org.bakeneko.rabbitmq.rpc.RabbitSender;

@RabbitClient(routingKeyGenerator = "defaultRoutingKeyGenerator")
public interface GeneratorTestingClient {
    @RabbitSender(routingKeyGenerator = "customRoutingKeyGenerator")
    String sendWithGeneratedRoutingKey(String payload);

    @RabbitSender
    String sendWithDefaultGeneratedRoutingKey(String payload);
}
