package com.msp.controllers;

import com.msp.payloads.response.ApiResponse2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/admin/cache")
@RequiredArgsConstructor
@Tag(name = "Cache Management", description = "Endpoints for monitoring and managing Redis cache")
@Slf4j
public class CacheController {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    @Operation(summary = "Get all cache statistics")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        // Get all cache names
        Collection<String> cacheNames = cacheManager.getCacheNames();
        stats.put("total_caches", cacheNames.size());
        stats.put("cache_names", cacheNames);

        // Detailed stats for each cache
        Map<String, Map<String, Object>> cacheDetails = new HashMap<>();

        for (String cacheName : cacheNames) {
            Map<String, Object> cacheInfo = new HashMap<>();

            // Count keys for this cache pattern
            Set<String> keys = redisTemplate.keys(cacheName + "*");
            cacheInfo.put("key_count", keys != null ? keys.size() : 0);

            // Sample first few keys
            if (keys != null && !keys.isEmpty()) {
                cacheInfo.put("sample_keys", keys.stream().limit(5).toList());
            }

            cacheDetails.put(cacheName, cacheInfo);
        }

        stats.put("cache_details", cacheDetails);

        // Redis server info
        Properties redisInfo = getRedisInfo();
        stats.put("redis_info", redisInfo);

        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Check specific cache entry")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN')")
    @GetMapping("/check/{cacheName}/{key}")
    public ResponseEntity<Map<String, Object>> checkCacheEntry(
            @PathVariable String cacheName,
            @PathVariable String key) {

        Map<String, Object> result = new HashMap<>();

        // Check if cache exists
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            result.put("error", "Cache '" + cacheName + "' not found");
            return ResponseEntity.badRequest().body(result);
        }

        // Try to get from cache
        Cache.ValueWrapper valueWrapper = cache.get(key);

        result.put("cache_name", cacheName);
        result.put("key", key);
        result.put("exists_in_cache", valueWrapper != null);

        if (valueWrapper != null) {
            result.put("value", valueWrapper.get());
            result.put("value_type", valueWrapper.get().getClass().getSimpleName());
        }

        // Also check via RedisTemplate
        String redisKey = cacheName + "::" + key;
        Boolean hasKey = redisTemplate.hasKey(redisKey);
        result.put("exists_in_redis", hasKey);

        if (Boolean.TRUE.equals(hasKey)) {
            result.put("redis_key", redisKey);
            result.put("ttl_seconds", redisTemplate.getExpire(redisKey, TimeUnit.SECONDS));
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Test specific endpoint caching")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN')")
    @GetMapping("/test/{entityType}/{id}")
    public ResponseEntity<Map<String, Object>> testEndpointCache(
            @PathVariable String entityType,
            @PathVariable String id) {

        Map<String, Object> result = new HashMap<>();
        String cacheName = getCacheNameForEntity(entityType);

        if (cacheName == null) {
            result.put("error", "Unknown entity type: " + entityType);
            return ResponseEntity.badRequest().body(result);
        }

        // Record first access time (simulate)
        long startTime = System.currentTimeMillis();

        // Check cache before
        String redisKey = cacheName + "::" + id;
        Boolean beforeExists = redisTemplate.hasKey(redisKey);

        result.put("entity_type", entityType);
        result.put("id", id);
        result.put("cache_name", cacheName);
        result.put("in_cache_before_request", beforeExists);

        if (Boolean.TRUE.equals(beforeExists)) {
            Object cachedValue = redisTemplate.opsForValue().get(redisKey);
            result.put("cached_value", cachedValue);
            result.put("ttl_seconds", redisTemplate.getExpire(redisKey, TimeUnit.SECONDS));
        }

        result.put("test_timestamp", new Date().toString());
        result.put("note", "To test caching, call this endpoint twice and compare response times");

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "List all keys in a cache")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN')")
    @GetMapping("/keys/{cacheName}")
    public ResponseEntity<Map<String, Object>> listCacheKeys(@PathVariable String cacheName) {
        Map<String, Object> result = new HashMap<>();

        Set<String> keys = redisTemplate.keys(cacheName + "*");

        if (keys == null || keys.isEmpty()) {
            result.put("message", "No keys found for cache: " + cacheName);
            return ResponseEntity.ok(result);
        }

        List<Map<String, Object>> keyDetails = new ArrayList<>();
        for (String key : keys) {
            Map<String, Object> keyInfo = new HashMap<>();
            keyInfo.put("key", key);
            keyInfo.put("ttl", redisTemplate.getExpire(key, TimeUnit.SECONDS));
            keyInfo.put("type", redisTemplate.type(key).code());
            keyDetails.add(keyInfo);
        }

        result.put("cache_name", cacheName);
        result.put("total_keys", keys.size());
        result.put("keys", keyDetails);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Clear specific cache entry")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN')")
    @DeleteMapping("/clear/{cacheName}/{key}")
    public ResponseEntity<ApiResponse2> clearCacheEntry(
            @PathVariable String cacheName,
            @PathVariable String key) {

        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }

        // Also evict via RedisTemplate
        String redisKey = cacheName + "::" + key;
        Boolean deleted = redisTemplate.delete(redisKey);

        ApiResponse2 response = new ApiResponse2();
        response.setMessage("Cache entry cleared: " + cacheName + "::" + key +
                (Boolean.TRUE.equals(deleted) ? " (deleted)" : " (not found)"));

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Clear all caches")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN')")
    @DeleteMapping("/clear/all")
    public ResponseEntity<ApiResponse2> clearAllCaches() {
        // Clear via CacheManager
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }

        // Clear all Redis keys
        Set<String> allKeys = redisTemplate.keys("*");
        if (allKeys != null && !allKeys.isEmpty()) {
            redisTemplate.delete(allKeys);
        }

        ApiResponse2 response = new ApiResponse2();
        response.setMessage("All caches cleared. Total keys removed: " +
                (allKeys != null ? allKeys.size() : 0));

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get cache hit/miss statistics")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN')")
    @GetMapping("/hit-miss")
    public ResponseEntity<Map<String, Object>> getHitMissStats() {
        Map<String, Object> stats = new HashMap<>();

        // Get Redis info stats
        Properties info = getRedisInfo();

        String hits = info.getProperty("keyspace_hits");
        String misses = info.getProperty("keyspace_misses");

        stats.put("keyspace_hits", hits != null ? Long.parseLong(hits) : 0);
        stats.put("keyspace_misses", misses != null ? Long.parseLong(misses) : 0);

        long total = (hits != null ? Long.parseLong(hits) : 0) +
                (misses != null ? Long.parseLong(misses) : 0);

        stats.put("total_requests", total);

        if (total > 0) {
            double hitRatio = (double) stats.get("keyspace_hits") / total;
            stats.put("hit_ratio", String.format("%.2f%%", hitRatio * 100));
        } else {
            stats.put("hit_ratio", "0%");
        }

        return ResponseEntity.ok(stats);
    }

    // Helper method to get cache name for entity type
    private String getCacheNameForEntity(String entityType) {
        return switch (entityType.toLowerCase()) {
            case "user" -> "users";
            case "branch" -> "branches";
            case "product" -> "products";
            case "category" -> "categories";
            case "order" -> "orders";
            case "inventory" -> "inventory";
            case "refund" -> "refunds";
            case "shift" -> "shifts";
            case "store" -> "stores";
            case "customer" -> "customers";
            default -> null;
        };
    }

    // Helper to get Redis info
    private Properties getRedisInfo() {
        Properties info = new Properties();
        try {
            // Get Redis server info
            Properties serverInfo = redisTemplate.getRequiredConnectionFactory()
                    .getConnection()
                    .serverCommands()
                    .info("stats");

            if (serverInfo != null) {
                info.putAll(serverInfo);
            }
        } catch (Exception e) {
            log.error("Failed to get Redis info", e);
            info.setProperty("error", "Could not connect to Redis");
        }
        return info;
    }
}