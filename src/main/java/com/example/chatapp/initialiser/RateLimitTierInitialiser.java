package com.example.chatapp.initialiser;

import com.example.chatapp.model.RateLimitTier;
import com.example.chatapp.repository.RateLimitTierRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class RateLimitTierInitialiser implements CommandLineRunner{
    private final RateLimitTierRepository repository;

    public RateLimitTierInitialiser(RateLimitTierRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        repository.count()
                .flatMap(count -> {
                    if (count == 0) {
                        log.info("Initializing rate limit tiers...");
                        return repository.saveAll(List.of(
                                        new RateLimitTier("free", 20, 1),
                                        new RateLimitTier("premium", 100, 1),
                                        new RateLimitTier("enterprise", 1000, 1)
                                )).collectList()
                                .doOnSuccess(tiers -> log.info("Initialized {} rate limit tiers", tiers.size()));
                    }
                    log.info("Rate limit tiers already exist, skipping initialization");
                    return Mono.empty();
                })
                .block();
    }

}
