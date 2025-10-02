package com.example.chatapp.integration;


import com.example.chatapp.integration.config.RedisTestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.context.annotation.Import;



@SpringBootTest
@Testcontainers
@Import(RedisTestConfig.class)
public abstract class BaseIntegrationTest {

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:alpine")
            .withExposedPorts(6379);

    @Container
    static final CassandraContainer<?> cassandra = new CassandraContainer<>("cassandra:3.11")
            .withInitScript("init-keyspace.cql");

//    @Container
//    static final MongoDBContainer mongodb = new MongoDBContainer("mongo:4.4")
//            .withExposedPorts(27017);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
//        dynamic container properties
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.cassandra.contact-points", cassandra::getHost);
        registry.add("spring.cassandra.port", cassandra::getFirstMappedPort);
//        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);


//        static test properties
        registry.add("server.id", () -> "localhost:8080");
        registry.add("spring.cassandra.local-datacenter", () -> "datacenter1");
        registry.add("spring.cassandra.keyspace-name", () -> "chatapp_test");


    }
}
