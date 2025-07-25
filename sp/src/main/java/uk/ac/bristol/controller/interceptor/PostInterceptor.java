package uk.ac.bristol.controller.interceptor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class PostInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, String> redisTemplate;

    public PostInterceptor(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // skip non-POST methods
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // skip requests with idempotent header
        String skipHeader = request.getHeader("X-idempotent-post");
        if ("true".equalsIgnoreCase(skipHeader)) {
            return true;
        }

        String keyHeader = request.getHeader("idempotent-key");
        if (keyHeader == null || keyHeader.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing idempotent-key header");
            return false;
        }

        try {
            UUID.fromString(keyHeader);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid UUID format");
            return false;
        }

        String redisKey = "uuid:" + keyHeader;
        if (redisTemplate.hasKey(redisKey)) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("Identical POST key detected, your request has been declined");
            return false;
        }

        redisTemplate.opsForValue().set(redisKey, "used", 5, TimeUnit.MINUTES);

        return true;
    }
}