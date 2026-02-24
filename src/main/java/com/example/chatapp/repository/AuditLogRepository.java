package com.example.chatapp.repository;

import com.example.chatapp.model.AuditLog;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends ReactiveCassandraRepository<AuditLog, UUID> {
}
