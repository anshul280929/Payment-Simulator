package com.payment.simulator.gateway.service;

import com.payment.simulator.common.event.PaymentEvent;
import com.payment.simulator.gateway.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishAuthorized(PaymentEvent event) {
        log.info("Publishing payment.authorized event: txn={}", event.getTransactionId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENT_EXCHANGE, "payment.authorized", event);
    }

    public void publishCaptured(PaymentEvent event) {
        log.info("Publishing payment.captured event: txn={}", event.getTransactionId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENT_EXCHANGE, "payment.captured", event);
    }

    public void publishRefunded(PaymentEvent event) {
        log.info("Publishing payment.refunded event: txn={}", event.getTransactionId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENT_EXCHANGE, "payment.refunded", event);
    }
}
