package com.learning.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@ConditionalOnProperty(
        prefix = "otp.cache",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class ValkeyConfig {

    @Bean
    public LettuceConnectionFactory valkeyConnectionFactory(
            @Value("${valkey.host}") String host,
            @Value("${valkey.port:6379}") int port,
            @Value("${valkey.password}") String password) {

        RedisStandaloneConfiguration server =
                new RedisStandaloneConfiguration(host, port);
        server.setPassword(RedisPassword.of(password));

        LettuceClientConfiguration client =
                LettuceClientConfiguration.builder()
                        .useSsl()
                        .build();

        return new LettuceConnectionFactory(server, client);
    }

    @Bean
    public StringRedisTemplate valkeyTemplate(
            LettuceConnectionFactory valkeyConnectionFactory) {
        return new StringRedisTemplate(valkeyConnectionFactory);
    }
}