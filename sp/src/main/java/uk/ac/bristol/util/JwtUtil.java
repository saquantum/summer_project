package uk.ac.bristol.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import uk.ac.bristol.controller.Code;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    private static String secretKey = System.getenv("JWT_SECRET_KEY");
    private static Long expirePeriod = 86400000L; // 1 day

    static {
        if (secretKey == null || secretKey.isBlank()) {
            secretKey = "testSecretKeyJWTWhichMustBeLongerThan32CharactersToSatisfyTheHS256Algorithm";
        }
    }

    public static String generateJWT(Map<String, Object> claims) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(new Date(System.currentTimeMillis() + expirePeriod))
                .compact();
    }

    public static Claims parseJWT(String jwt) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(key)
                .build();
        return parser.parseClaimsJws(jwt).getBody();
    }

    public static String getJWTFromCookie(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        response.setStatus(Code.LOGIN_TOKEN_ERR);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401, \"message\":\"Missing or empty token\"}");
        return null;
    }
}
