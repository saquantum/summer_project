package uk.ac.bristol.controller.interceptor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
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
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uuidHeader = request.getHeader("UUID");
        if (uuidHeader == null || uuidHeader.isEmpty()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write("Missing UUID header");
            return false;
        }

        try {
            UUID.fromString(uuidHeader);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write("Invalid UUID format");
            return false;
        }

        String redisKey = "uuid:" + uuidHeader;
        Boolean exists = redisTemplate.hasKey(redisKey);
        if (Boolean.TRUE.equals(exists)) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write("UUID already exists");
            return false;
        }

        redisTemplate.opsForValue().set(redisKey, "used", 1, TimeUnit.HOURS);

        return true;
    }
}