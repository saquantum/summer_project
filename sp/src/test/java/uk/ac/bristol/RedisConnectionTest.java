package uk.ac.bristol;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RedisConnectionTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void testRedisConnection() {
        String key = "connection_test_key";
        String value = "success";

        redisTemplate.opsForValue().set(key, value);
        String result = redisTemplate.opsForValue().get(key);

        assertThat(result).isEqualTo(value);
        System.out.println("Redis connection successful, value = " + result);
    }
}

