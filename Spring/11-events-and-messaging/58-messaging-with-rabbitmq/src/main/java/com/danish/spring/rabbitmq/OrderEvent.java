package com.danish.spring.rabbitmq;

// Deliberately NOT Serializable - a plain record. See RabbitConfig and the .md for the
// real MessageConversionException this causes with Spring AMQP's default converter.
public record OrderEvent(String orderId, String status) {
}
