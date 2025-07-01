package uk.ac.bristol.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    @Test
    public void testGenerateAndParseJWT() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", "user_001");
        claims.put("password", "123456");
        claims.put("isAdmin", false);

        Date now = new Date();
        String token = JwtUtil.generateJWT(claims);
        assertNotNull(token);

        Claims parsed = JwtUtil.parseJWT(token);
        assertEquals("user_001", parsed.get("id"));
        assertEquals("123456", parsed.get("password"));
        assertEquals(false, parsed.get("isAdmin"));
        assertTrue(parsed.getExpiration().after(now));
    }

    @Test
    public void testGetJWTFromCookie() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setCookies(new Cookie("token", "abc123"));

        String token = JwtUtil.getJWTFromCookie(request);
        assertEquals("abc123", token);

        request = new MockHttpServletRequest();

        token = JwtUtil.getJWTFromCookie(request);

        assertNull(token);
    }

    @Test
    public void testRefreshJWT() throws InterruptedException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", "user_001");

        String token = JwtUtil.generateJWT(claims);
        Claims original = JwtUtil.parseJWT(token);

        Thread.sleep(1000L);

        String refreshedToken = JwtUtil.refreshJWT(token);
        Claims refreshed = JwtUtil.parseJWT(refreshedToken);

        assertEquals("user_001", refreshed.get("id"));
        assertTrue(refreshed.getExpiration().after(original.getExpiration()));
    }
}
