package com.example.urlshortener.dto;

public record LoginResponse(
        String token,
        String username
) {}
