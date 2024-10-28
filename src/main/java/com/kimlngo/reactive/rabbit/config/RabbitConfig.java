package com.kimlngo.reactive.rabbit.config;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Delivery;
import jakarta.annotation.PreDestroy;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.*;

@Configuration
public class RabbitConfig {
    public static final String QUEUE = "quotes";

    @Autowired
    Mono<Connection> connectionMono;

    @Bean
    Mono<Connection> connectionMono(CachingConnectionFactory connectionFactory) {
        return Mono.fromCallable(() -> connectionFactory.getRabbitConnectionFactory()
                                                        .newConnection());
    }

    @PreDestroy
    public void close() throws Exception {
        connectionMono.block().close();
    }

    @Bean
    Sender sender(Mono<Connection> mono) {
        return RabbitFlux.createSender(new SenderOptions().connectionMono(mono));
    }

    @Bean
    Receiver receiver(Mono<Connection> mono) {
        return RabbitFlux.createReceiver(new ReceiverOptions().connectionMono(mono));
    }
}
