package com.example.chatapp.integration.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.CassandraContainer;
import java.time.Duration;

public class CassandraTestConfig {
    
    public static CassandraContainer<?> createCassandraContainer() {
        return new CassandraContainer<>("cassandra:3.11")
                .withInitScript("init-keyspace.cql")
                .withStartupTimeout(Duration.ofMinutes(5))
                .withReuse(false)
                .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forListeningPort());
    }
    
    public static void configureCassandra(DynamicPropertyRegistry registry, CassandraContainer<?> cassandra) {
        registry.add("spring.cassandra.contact-points", cassandra::getHost);
        registry.add("spring.cassandra.port", cassandra::getFirstMappedPort);
        registry.add("spring.cassandra.request.timeout", () -> "10s");
        registry.add("spring.cassandra.connection.connect-timeout", () -> "10s");
        registry.add("spring.cassandra.connection.init-query-timeout", () -> "10s");
        registry.add("spring.cassandra.local-datacenter", () -> "datacenter1");
        registry.add("spring.cassandra.keyspace-name", () -> "chatapp_test");
        registry.add("server.id", () -> "localhost:8080");
    }
}
