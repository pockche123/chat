package com.example.chatapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("rate_limit_tiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitTier {
    @Id
    private String tier;
    private int maxRequests;
    private int windowMinutes;
}
