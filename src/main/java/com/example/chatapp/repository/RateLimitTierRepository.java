package com.example.chatapp.repository;

import com.example.chatapp.model.RateLimitTier;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RateLimitTierRepository extends ReactiveCassandraRepository<RateLimitTier, String> {
}
