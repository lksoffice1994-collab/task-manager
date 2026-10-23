package com.learning.valkey.impl;

import com.learning.valkey.ValkeyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;

@Service
@ConditionalOnProperty(
        prefix = "otp.cache",
        name = "enabled",
        havingValue = "true"
)
public class ValkeyServiceImpl implements ValkeyService {

    private static final String OTP_KEY_PREFIX = "otp:";

    private final StringRedisTemplate valkeyTemplate;
    private final long expirationTimeInSeconds;

    public ValkeyServiceImpl(
            StringRedisTemplate valkeyTemplate,
            @Value("${otp.cache.expiration-seconds:900}") long expirationTimeInSeconds) {
        this.valkeyTemplate = valkeyTemplate;
        this.expirationTimeInSeconds = expirationTimeInSeconds;
    }

    @Override
    public void saveOtp(String email, String otp) {
        String key = otpKey(email);
        valkeyTemplate.opsForValue().set(
                key,
                otp,
                Duration.ofSeconds(expirationTimeInSeconds)
        );
    }

    @Override
    public void deleteOtp(String email) {
        String key = otpKey(email);
        valkeyTemplate.delete(key);
    }

    @Override
    public String getOtp(String email) {
        return valkeyTemplate.opsForValue().get(otpKey(email));
    }

    private String otpKey(String email) {
        return OTP_KEY_PREFIX + email.trim().toLowerCase(Locale.ROOT);
    }
}