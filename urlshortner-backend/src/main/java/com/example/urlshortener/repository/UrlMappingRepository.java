package com.example.urlshortener.repository;

import com.example.urlshortener.models.UrlMapping;
import com.example.urlshortener.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, String> {
    List<UrlMapping> findByUser(User user);
}
