package com.ppaw.passwordvault.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LAB 8: Simple token service for authentication
 * In-memory token storage (for production, use Redis or JWT)
 */
@Service
public class TokenService {

    // In-memory storage: token -> userId
    private final Map<String, Long> tokenStore = new ConcurrentHashMap<>();

    /**
     * Generate a new token for a user
     */
    public String generateToken(Long userId) {
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, userId);
        return token;
    }

    /**
     * Validate token and return userId if valid
     */
    public Long validateToken(String token) {
        return tokenStore.get(token);
    }

    /**
     * Invalidate a token (logout)
     */
    public void invalidateToken(String token) {
        tokenStore.remove(token);
    }

    /**
     * Check if token exists
     */
    public boolean tokenExists(String token) {
        return tokenStore.containsKey(token);
    }
}

