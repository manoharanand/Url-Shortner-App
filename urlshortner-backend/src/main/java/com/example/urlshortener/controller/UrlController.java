package com.example.urlshortener.controller;

import com.example.urlshortener.models.UrlMapping;
import com.example.urlshortener.models.User;
import com.example.urlshortener.service.UrlShortenerService;
import com.example.urlshortener.service.JwtTokenProvider;
import com.example.urlshortener.service.AuthService;
import com.example.urlshortener.dto.LoginRequest;
import com.example.urlshortener.dto.LoginResponse;
import com.example.urlshortener.dto.RegisterRequest;
import com.example.urlshortener.dto.RegisterResponse;
import com.example.urlshortener.dto.UserUrlResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class UrlController {

    private final UrlShortenerService urlShortenerService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    public UrlController(UrlShortenerService urlShortenerService, JwtTokenProvider jwtTokenProvider, AuthService authService) {
        this.urlShortenerService = urlShortenerService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authService = authService;
    }

    @PostMapping("/api/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.registerUser(request.email(), request.password());
            return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponse("User registered successfully", user.getEmail()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse("Bad Request", e.getMessage(), Instant.now())
            );
        }
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Optional<User> user = authService.authenticateUser(request.email(), request.password());
        if (user.isPresent()) {
            String token = jwtTokenProvider.generateToken(user.get().getEmail());
            return ResponseEntity.ok(new LoginResponse(token, user.get().getEmail()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse("Unauthorized", "Invalid email or password", Instant.now())
        );
    }

    @PostMapping("/api/urls")
    public ResponseEntity<CreateUrlResponse> create(@Valid @RequestBody CreateUrlRequest request, HttpServletRequest servletRequest) {
        // Check if user is authenticated
        String email = (String) servletRequest.getAttribute("username");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> maybeUser = authService.findByEmail(email);
        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = maybeUser.get();

        Instant expiresAt = null;
        if (request.expirationDate() != null && !request.expirationDate().isBlank()) {
            expiresAt = Instant.parse(request.expirationDate());
        }

        UrlMapping mapping = urlShortenerService.createShortUrl(
                request.longUrl(),
                request.customAlias(),
                expiresAt,
                user
        );

        String shortUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/")
                .path(mapping.getShortCode())
                .toUriString();
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateUrlResponse(shortUrl));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletResponse response) {
        Optional<UrlMapping> maybeMapping = urlShortenerService.getByCode(shortCode);
        if (maybeMapping.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        UrlMapping mapping = maybeMapping.get();
        if (mapping.isExpired(Instant.now())) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(mapping.getLongUrl()))
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }

    @GetMapping("/api/user-urls")
    public ResponseEntity<?> getUserUrls(HttpServletRequest servletRequest) {
        // Check if user is authenticated
        String email = (String) servletRequest.getAttribute("username");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> maybeUser = authService.findByEmail(email);
        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = maybeUser.get();
        List<UrlMapping> userUrls = urlShortenerService.getUserUrls(user);

        List<UserUrlResponse> responses = userUrls.stream()
                .map(mapping -> {
                    String shortUrl = ServletUriComponentsBuilder
                            .fromCurrentContextPath()
                            .path("/")
                            .path(mapping.getShortCode())
                            .toUriString();
                    return new UserUrlResponse(
                            mapping.getShortCode(),
                            mapping.getLongUrl(),
                            mapping.getCreatedAt(),
                            mapping.getExpiresAt(),
                            shortUrl
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}
