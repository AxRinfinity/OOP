package com.library.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A simple in-memory cache implementation using ConcurrentHashMap.
 * @param <K> The type of the cache key
 * @param <V> The type of the cached value
 */
public class Cache<K, V> {
    private final Map<K, CacheEntry<V>> cache;
    private final long timeToLiveMs;

    public Cache(long timeToLiveSeconds) {
        this.cache = new ConcurrentHashMap<>();
        this.timeToLiveMs = timeToLiveSeconds * 1000;
    }

    /**
     * Gets a value from cache or computes it using the provided function if not present or expired.
     * @param key The cache key
     * @param loader Function to compute the value if not in cache
     * @return The cached or computed value
     */
    public V get(K key, Function<K, V> loader) {
        CacheEntry<V> entry = cache.get(key);
        
        if (entry == null || entry.isExpired()) {
            V value = loader.apply(key);
            put(key, value);
            return value;
        }
        
        return entry.getValue();
    }

    /**
     * Puts a value in the cache with the current timestamp.
     * @param key The cache key
     * @param value The value to cache
     */
    public void put(K key, V value) {
        cache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + timeToLiveMs));
    }

    /**
     * Removes a value from the cache.
     * @param key The cache key to remove
     */
    public void invalidate(K key) {
        cache.remove(key);
    }

    /**
     * Clears all entries from the cache.
     */
    public void invalidateAll() {
        cache.clear();
    }

    /**
     * Inner class to hold cache entries with expiration time.
     */
    private static class CacheEntry<V> {
        private final V value;
        private final long expirationTime;

        public CacheEntry(V value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        public V getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
} 