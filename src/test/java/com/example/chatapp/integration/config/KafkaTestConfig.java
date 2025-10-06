package com.example.chatapp.integration.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import java.time.Duration;

public class KafkaTestConfig {
    
    private static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withStartupTimeout(Duration.ofMinutes(2));
    
    static {
        kafka.start();
        System.setProperty("spring.kafka.bootstrap-servers", kafka.getBootstrapServers());
    }
    
    public static KafkaContainer createKafkaContainer() {
        return kafka;
    }
    
    public static void configureKafka(DynamicPropertyRegistry registry, KafkaContainer kafka) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", kafka::getBootstrapServers);
    }
}
