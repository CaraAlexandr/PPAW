package com.ppaw.passwordvault.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Memory Cache Service Implementation
 * Provides in-memory caching with TTL (Time To Live) support
 */
@Slf4j
@Service
public class MemoryCacheService implements CacheService {

    private static final int DEFAULT_CACHE_TIME_MINUTES = 60;
    
    /**
     * Cache entry wrapper that includes expiration time
     */
    private static class CacheEntry {
        private final Object value;
        private final LocalDateTime expirationTime;
        
        public CacheEntry(Object value, LocalDateTime expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }
        
        public Object getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expirationTime);
        }
    }
    
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final int defaultCacheTimeMinutes;

    public MemoryCacheService() {
        this(DEFAULT_CACHE_TIME_MINUTES);
    }

    public MemoryCacheService(int defaultCacheTimeMinutes) {
        this.defaultCacheTimeMinutes = defaultCacheTimeMinutes;
        log.info("MemoryCacheService initialized with default cache time: {} minutes", defaultCacheTimeMinutes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        CacheEntry entry = cache.get(key);
        
        if (entry == null) {
            log.debug("Cache miss for key: {}", key);
            return null;
        }
        
        if (entry.isExpired()) {
            log.debug("Cache entry expired for key: {}", key);
            cache.remove(key);
            return null;
        }
        
        log.debug("Cache hit for key: {}", key);
        try {
            Object value = entry.getValue();
            if (type.isInstance(value)) {
                return (T) value;
            } else {
                log.warn("Cache value type mismatch for key: {}, expected: {}, got: {}", 
                        key, type.getName(), value.getClass().getName());
                cache.remove(key);
                return null;
            }
        } catch (Exception e) {
            log.warn("Error casting cache value for key: {}", key, e);
            cache.remove(key);
            return null;
        }
    }

    @Override
    public void set(String key, Object objectData, Integer cacheTimeMinutes) {
        if (objectData == null) {
            log.debug("Attempted to cache null value for key: {}, skipping", key);
            return;
        }
        
        int timeToCache = cacheTimeMinutes != null ? cacheTimeMinutes : defaultCacheTimeMinutes;
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(timeToCache);
        
        cache.put(key, new CacheEntry(objectData, expirationTime));
        log.debug("Cached value for key: {} with expiration: {}", key, expirationTime);
    }

    @Override
    public boolean isSet(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return false;
        }
        
        if (entry.isExpired()) {
            cache.remove(key);
            return false;
        }
        
        return true;
    }

    @Override
    public void remove(String key) {
        CacheEntry removed = cache.remove(key);
        if (removed != null) {
            log.debug("Removed cache entry for key: {}", key);
        }
    }

    @Override
    public void removeByPattern(String pattern) {
        int removedCount = 0;
        for (String key : cache.keySet()) {
            if (key.startsWith(pattern)) {
                cache.remove(key);
                removedCount++;
            }
        }
        if (removedCount > 0) {
            log.debug("Removed {} cache entries matching pattern: {}", removedCount, pattern);
        }
    }

    @Override
    public void clear() {
        int size = cache.size();
        cache.clear();
        log.info("Cleared all cache entries ({} entries removed)", size);
    }
    
    /**
     * Gets the current cache size (for monitoring/debugging)
     * @return The number of entries in the cache
     */
    public int getCacheSize() {
        // Clean up expired entries first
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        return cache.size();
    }
}

