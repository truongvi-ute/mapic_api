package com.mapic.backend.service;

import com.mapic.backend.dto.RegisterRequest;
import com.mapic.backend.entity.OtpType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements IOtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String OTP_KEY_PREFIX = "otp:%s:%s";
    private static final String REG_KEY_PREFIX = "pending-reg:%s";
    private static final long OTP_TTL_MINUTES = 5;
    private static final long REG_TTL_MINUTES = 15;

    @Override
    public String generateAndStoreOtp(String email, OtpType type) {
        String code = String.format("%06d", new Random().nextInt(999999));
        String key = String.format(OTP_KEY_PREFIX, type.name(), email);
        
        redisTemplate.opsForValue().set(key, code, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        return code;
    }

    @Override
    public boolean validateOtp(String email, String code, OtpType type) {
        String key = String.format(OTP_KEY_PREFIX, type.name(), email);
        String storedCode = (String) redisTemplate.opsForValue().get(key);
        return code != null && code.equals(storedCode);
    }

    @Override
    public void deleteOtp(String email, OtpType type) {
        String key = String.format(OTP_KEY_PREFIX, type.name(), email);
        redisTemplate.delete(key);
    }

    @Override
    public void savePendingRegistration(String email, RegisterRequest data) {
        String key = String.format(REG_KEY_PREFIX, email);
        redisTemplate.opsForValue().set(key, data, REG_TTL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public Optional<RegisterRequest> getPendingRegistration(String email) {
        String key = String.format(REG_KEY_PREFIX, email);
        RegisterRequest data = (RegisterRequest) redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(data);
    }

    @Override
    public void deletePendingRegistration(String email) {
        String key = String.format(REG_KEY_PREFIX, email);
        redisTemplate.delete(key);
    }
}
