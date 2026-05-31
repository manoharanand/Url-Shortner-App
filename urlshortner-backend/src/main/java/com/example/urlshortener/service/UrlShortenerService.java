package com.example.urlshortener.service;

import com.example.urlshortener.models.UrlMapping;
import com.example.urlshortener.models.User;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@CacheConfig(cacheNames = "urlByCode")
public class UrlShortenerService {

    private static final Pattern ALIAS_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{4,32}$");
    private static final int MAX_GENERATION_RETRIES = 5;

    private final UrlMappingRepository repository;
    private final CounterService counterService;
    private final Base62Encoder base62Encoder;

    public UrlShortenerService(UrlMappingRepository repository, CounterService counterService, Base62Encoder base62Encoder) {
        this.repository = repository;
        this.counterService = counterService;
        this.base62Encoder = base62Encoder;
    }

    @Transactional
    public UrlMapping createShortUrl(String longUrl, String customAlias, Instant expiresAt, User user) {
        validateLongUrl(longUrl);
        validateExpiry(expiresAt);

        if (customAlias != null && !customAlias.isBlank()) {
            validateAlias(customAlias);
            return createWithAlias(longUrl, customAlias, expiresAt, user);
        }

        for (int attempt = 0; attempt < MAX_GENERATION_RETRIES; attempt++) {
            String shortCode = base62Encoder.encode(counterService.next());
            try {
                UrlMapping mapping = new UrlMapping(shortCode, longUrl, Instant.now(), expiresAt, user);
                return repository.save(mapping);
            } catch (DataIntegrityViolationException ignored) {
                // Rare collision or racing write. Retry with a fresh code.
            }
        }
        throw new ShortCodeGenerationException("Unable to generate unique short code after retries");
    }

    @Cacheable(key = "#shortCode", unless = "#result == null")
    public Optional<UrlMapping> getByCode(String shortCode) {
        System.out.println("DB lookup for shortcode: " + shortCode);
        return repository.findById(shortCode);
    }

    public List<UrlMapping> getUserUrls(User user) {
        return repository.findByUser(user);
    }

    @Transactional
    protected UrlMapping createWithAlias(String longUrl, String alias, Instant expiresAt, User user) {
        UrlMapping mapping = new UrlMapping(alias, longUrl, Instant.now(), expiresAt, user);
        try {
            return repository.save(mapping);
        } catch (DataIntegrityViolationException ex) {
            throw new AliasAlreadyExistsException("Alias already exists: " + alias);
        }
    }

    private void validateLongUrl(String longUrl) {
        if (longUrl == null || longUrl.isBlank()) {
            throw new IllegalArgumentException("longUrl is required");
        }
        try {
            URI uri = new URI(longUrl);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("URL must use http or https");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("URL must include a host");
            }
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Invalid URL syntax");
        }
    }

    private void validateAlias(String alias) {
        if (!ALIAS_PATTERN.matcher(alias).matches()) {
            throw new IllegalArgumentException("Alias must match [a-zA-Z0-9_-]{4,32}");
        }
    }

    private void validateExpiry(Instant expiresAt) {
        if (expiresAt != null && !expiresAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("Expiration must be in the future");
        }
    }
}
