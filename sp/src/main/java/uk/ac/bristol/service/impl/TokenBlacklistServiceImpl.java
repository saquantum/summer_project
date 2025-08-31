package uk.ac.bristol.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import uk.ac.bristol.service.TokenBlacklistService;
import uk.ac.bristol.util.JwtUtil;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public TokenBlacklistServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklistToken(String token) {
        long seconds = JwtUtil.getRemainingSeconds(token);
        if (seconds > 0) {
            String redisKey = "blacklist:" + token;
            redisTemplate.opsForValue().set(redisKey, "blacklisted", seconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        String redisKey = "blacklist:" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
    }
}
