package com.example.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record UserUrlResponse(
        @JsonProperty("short_code")
        String shortCode,
        @JsonProperty("long_url")
        String longUrl,
        @JsonProperty("created_at")
        Instant createdAt,
        @JsonProperty("expires_at")
        Instant expiresAt,
        @JsonProperty("short_url")
        String shortUrl
) {
}
