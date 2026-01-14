package com.ppaw.passwordvault.cache;

/**
 * Cache Service Interface
 * Provides methods for caching objects in memory
 */
public interface CacheService {
    
    /**
     * Gets the value associated with the specified key.
     * @param key The cache key
     * @param type The class type of the value
     * @return The cached value, or null if not found
     */
    <T> T get(String key, Class<T> type);
    
    /**
     * Adds the specified key and object to the cache.
     * @param key The cache key
     * @param objectData The object to cache
     * @param cacheTimeMinutes Optional cache time in minutes (uses default if null)
     */
    void set(String key, Object objectData, Integer cacheTimeMinutes);
    
    /**
     * Checks if a value associated with the specified key is cached.
     * @param key The cache key
     * @return true if the key exists in cache, false otherwise
     */
    boolean isSet(String key);
    
    /**
     * Removes the value with the specified key from the cache.
     * @param key The cache key
     */
    void remove(String key);
    
    /**
     * Removes all cache entries whose keys start with the specified pattern.
     * @param pattern The pattern to match
     */
    void removeByPattern(String pattern);
    
    /**
     * Clears all cache data.
     */
    void clear();
}

