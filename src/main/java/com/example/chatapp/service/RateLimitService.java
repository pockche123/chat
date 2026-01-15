package com.example.chatapp.service;

import com.example.chatapp.model.RateLimitTier;
import com.example.chatapp.repository.RateLimitTierRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RateLimitService {
    private final RateLimitTierRepository rateLimitTierRepository;

    public RateLimitService(RateLimitTierRepository rateLimitTierRepository) {
        this.rateLimitTierRepository = rateLimitTierRepository;
    }

    public Mono<RateLimitTier> getRateLimit(String tier){
        return rateLimitTierRepository.findById(tier).defaultIfEmpty(new RateLimitTier("free", 20, 1));

    }
}
