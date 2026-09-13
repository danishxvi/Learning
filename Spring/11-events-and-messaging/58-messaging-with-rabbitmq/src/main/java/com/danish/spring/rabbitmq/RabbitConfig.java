package com.danish.spring.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Declaring Queue/TopicExchange/Binding as beans is enough - Spring Boot's
// RabbitAutoConfiguration wires in a RabbitAdmin that declares all of them against the
// real broker on startup. No manual "create the queue" step needed.
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "orders.exchange";
    public static final String ALL_QUEUE = "orders.all.queue";
    public static final String CREATED_QUEUE = "orders.created.queue";
    public static final String SHIPPED_QUEUE = "orders.shipped.queue";

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue allQueue() {
        return new Queue(ALL_QUEUE);
    }

    @Bean
    public Queue createdQueue() {
        return new Queue(CREATED_QUEUE);
    }

    @Bean
    public Queue shippedQueue() {
        return new Queue(SHIPPED_QUEUE);
    }

    // "order.#" matches ANY routing key starting with "order." (any number of words
    // after the dot) - this queue receives every order event regardless of status.
    @Bean
    public Binding allBinding() {
        return BindingBuilder.bind(allQueue()).to(ordersExchange()).with("order.#");
    }

    // "order.created" matches ONLY that exact routing key.
    @Bean
    public Binding createdBinding() {
        return BindingBuilder.bind(createdQueue()).to(ordersExchange()).with("order.created");
    }

    @Bean
    public Binding shippedBinding() {
        return BindingBuilder.bind(shippedQueue()).to(ordersExchange()).with("order.shipped");
    }
}
