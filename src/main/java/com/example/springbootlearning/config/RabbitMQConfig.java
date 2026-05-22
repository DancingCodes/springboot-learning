package com.example.springbootlearning.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "transfer.exchange";
    public static final String QUEUE = "transfer.log.queue";
    public static final String ROUTING_KEY = "transfer.log";

    @Bean
    public DirectExchange transferExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue transferLogQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding transferLogBinding() {
        return BindingBuilder.bind(transferLogQueue()).to(transferExchange()).with(ROUTING_KEY);
    }
}
