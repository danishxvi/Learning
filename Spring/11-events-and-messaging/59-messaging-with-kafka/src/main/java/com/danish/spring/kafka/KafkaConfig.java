package com.danish.spring.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC = "orders.topic";

    // A NewTopic bean is enough for Spring Boot's KafkaAdmin to create it against the
    // real broker on startup - 3 partitions so this lesson has room to show messages
    // spreading across partitions and being shared between consumers in a group.
    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name(TOPIC).partitions(3).replicas(1).build();
    }
}
