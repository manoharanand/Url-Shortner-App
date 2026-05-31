package com.example.urlshortener.controller;

import java.time.Instant;

public record ErrorResponse(String error, String message, Instant timestamp) {}
