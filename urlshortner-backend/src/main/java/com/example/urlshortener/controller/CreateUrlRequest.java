package com.example.urlshortener.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateUrlRequest(
        @JsonProperty("long_url")
        String longUrl,
        @JsonProperty("custom_alias")
        String customAlias,
        @JsonProperty("expiration_date")
        String expirationDate
) {
}
