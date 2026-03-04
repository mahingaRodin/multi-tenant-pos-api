package com.msp.configs;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.hibernate7.Hibernate7Module;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@EnableCaching
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class RedisConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jackson2JsonRedisSerializer()));
    }

    @Primary
    @Bean(name = "cacheManager")
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration())
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisSerializer<Object> jackson2JsonRedisSerializer() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();

        Hibernate7Module hibernateModule = new Hibernate7Module();
        // Prevent Jackson from forcing lazy-loaded relations to load during caching
        hibernateModule.disable(Hibernate7Module.Feature.FORCE_LAZY_LOADING);

        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(hibernateModule)
                .addModule(new SimpleModule("PageModule")
                        .addDeserializer(Page.class, new PageDeserializer())
                        .addDeserializer(PageImpl.class, new PageImplDeserializer()))
                .activateDefaultTypingAsProperty(
                        ptv,
                        ObjectMapper.DefaultTyping.NON_FINAL,
                        "@class")
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        return new JacksonRedisSerializer(mapper);
    }

    /**
     * Custom RedisSerializer implementation to replace deprecated
     * GenericJackson2JsonRedisSerializer
     */
    public static class JacksonRedisSerializer implements RedisSerializer<Object> {
        private final ObjectMapper objectMapper;

        public JacksonRedisSerializer(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public byte[] serialize(Object value) throws SerializationException {
            if (value == null) {
                return new byte[0];
            }
            try {
                return objectMapper.writeValueAsBytes(value);
            } catch (IOException e) {
                throw new SerializationException("Could not serialize object: " + e.getMessage(), e);
            }
        }

        @Override
        public Object deserialize(byte[] bytes) throws SerializationException {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                return objectMapper.readValue(bytes, Object.class);
            } catch (IOException e) {
                throw new SerializationException("Could not deserialize object: " + e.getMessage(), e);
            }
        }
    }

    public static class PageDeserializer extends JsonDeserializer<Page<?>> {
        @Override
        public Page<?> deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            JsonNode node = mapper.readTree(p);

            // Get content
            JsonNode contentNode = node.get("content");
            List<Object> content = new ArrayList<>();
            if (contentNode != null) {
                // Let Jackson deserializer figure out the types based on @class properties
                Object parsedContent = mapper.treeToValue(contentNode, Object.class);
                if (parsedContent instanceof List) {
                    content = (List<Object>) parsedContent;
                }
            }

            // Get page info
            int number = node.has("number") ? node.get("number").asInt() : 0;
            if (node.has("pageable") && node.get("pageable").has("pageNumber")) {
                number = node.get("pageable").get("pageNumber").asInt();
            }

            int size = node.has("size") ? node.get("size").asInt() : (content.isEmpty() ? 10 : content.size());
            if (node.has("pageable") && node.get("pageable").has("pageSize")) {
                size = node.get("pageable").get("pageSize").asInt();
            }

            long totalElements = node.has("totalElements") ? node.get("totalElements").asLong() : content.size();

            return new PageImpl<>(content, PageRequest.of(number, Math.max(size, 1)), totalElements);
        }
    }

    public static class PageImplDeserializer extends JsonDeserializer<PageImpl<?>> {
        @Override
        public PageImpl<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return (PageImpl<?>) new PageDeserializer().deserialize(p, ctxt);
        }
    }
}