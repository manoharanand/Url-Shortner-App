package com.example.urlshortener.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateUrlResponse(
        @JsonProperty("short_url")
        String shortUrl
) {
}
