package org.dev.hehe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 설정
 * - StringRedisTemplate 빈 등록 (refresh token 저장에 사용)
 */
@Configuration
public class RedisConfig {

    /**
     * 문자열 기반 Redis 템플릿
     * - key: RT:{userId}, value: refreshToken (TTL 14일)
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}