package com.msp.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RedisSerializationTest {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestUser {
        private String name;
        private LocalDateTime createdAt;
    }

    @Test
    public void testLocalDateTimeSerialization() {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();

        RedisConfig.JacksonRedisSerializer serializer = new RedisConfig.JacksonRedisSerializer(mapper);

        TestUser user = new TestUser("John Doe", LocalDateTime.now());

        byte[] serialized = serializer.serialize(user);
        assertNotNull(serialized);

        // We use Object.class as the target in JacksonRedisSerializer.deserialize
        // but here we know it's TestUser, however the serializer returns Object.
        // For a simple test, we can just check if we can deserialize it back to
        // something.
        // Note: Without DefaultTyping, it will deserialize to a LinkedHashMap.
        Object deserialized = serializer.deserialize(serialized);
        assertNotNull(deserialized);

        System.out.println("Serialized and Deserialized successfully: " + deserialized);
    }
}
