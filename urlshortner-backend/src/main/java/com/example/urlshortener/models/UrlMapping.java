package com.example.urlshortener.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "url_mappings")
@Data
@NoArgsConstructor
public class UrlMapping {
    @Id
    @Column(name = "short_code", nullable = false, length = 64)
    private String shortCode;

    @Column(name = "long_url", nullable = false, length = 4096)
    private String longUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @ManyToOne
    @JoinColumn(name = "user_email", nullable = true)
    private User user;

    public UrlMapping(String shortCode, String longUrl, Instant createdAt, Instant expiresAt, User user) {
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.user = user;
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }
}

