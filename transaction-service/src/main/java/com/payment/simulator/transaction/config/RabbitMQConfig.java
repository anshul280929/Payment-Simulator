package com.payment.simulator.transaction.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_AUTHORIZED_QUEUE = "payment.authorized.queue";
    public static final String PAYMENT_CAPTURED_QUEUE = "payment.captured.queue";
    public static final String PAYMENT_REFUNDED_QUEUE = "payment.refunded.queue";

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue authorizedQueue() {
        return QueueBuilder.durable(PAYMENT_AUTHORIZED_QUEUE).build();
    }

    @Bean
    public Queue capturedQueue() {
        return QueueBuilder.durable(PAYMENT_CAPTURED_QUEUE).build();
    }

    @Bean
    public Queue refundedQueue() {
        return QueueBuilder.durable(PAYMENT_REFUNDED_QUEUE).build();
    }

    @Bean
    public Binding authorizedBinding(Queue authorizedQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(authorizedQueue).to(paymentExchange).with("payment.authorized");
    }

    @Bean
    public Binding capturedBinding(Queue capturedQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(capturedQueue).to(paymentExchange).with("payment.captured");
    }

    @Bean
    public Binding refundedBinding(Queue refundedQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(refundedQueue).to(paymentExchange).with("payment.refunded");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
