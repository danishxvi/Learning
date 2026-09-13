package com.danish.spring.rabbitmq;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Spring Boot's RabbitAutoConfiguration wires ANY MessageConverter bean into both the
// RabbitTemplate (for sending) and the default listener container factory (for
// receiving) automatically - no need to configure either by hand. Without this bean,
// RabbitTemplate falls back to SimpleMessageConverter, which requires
// java.io.Serializable payloads - see the .md for the real exception a plain record
// (not Serializable) causes against that default.
@Configuration
public class RabbitMessageConverterConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
