# URL Shortener Backend

Spring Boot backend for URL Shortener application with JWT authentication.

## Technologies
- Java 17
- Spring Boot 3.3.0
- MySQL
- Redis (optional caching)
- JWT Authentication
- Spring Security

## Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- (Optional) Redis

## Setup

### 1. Database Setup
```sql
CREATE DATABASE url_shortner_db;
```

### 2. Application Properties
Update `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/url_shortner_db
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

Server runs on `http://localhost:8080`

## API Endpoints

### Authentication
- `POST /api/register` - Register new user
- `POST /api/login` - Login user

### URL Management
- `POST /api/urls` - Create short URL (requires JWT)
- `GET /{shortCode}` - Redirect to long URL
- `GET /api/user-urls` - Get user's created URLs (requires JWT)

## Features
- User registration and authentication
- JWT token-based authorization
- Short URL creation with optional custom alias
- URL expiration support
- User-specific URL management
- Redis caching (with fallback to in-memory cache)
