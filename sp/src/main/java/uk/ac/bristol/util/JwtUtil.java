package uk.ac.bristol.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JwtUtil {

    private static String secretKey = System.getenv("JWT_SECRET_KEY");
    private static Long expirePeriod = 86400000L; // 1 day
    private static Key key;

    private JwtUtil() {
        throw new IllegalStateException("Utility class");
    }

    static {
        if (secretKey == null || secretKey.isBlank()) {
            secretKey = "testSecretKeyJWTWhichMustBeLongerThan32CharactersToSatisfyTheHS256Algorithm";
        }
        key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateJWT(Map<String, Object> claims) {
        return Jwts.builder()
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(new Date(System.currentTimeMillis() + expirePeriod))
                .compact();
    }

    public static Claims parseJWT(String jwt) {
        if (jwt == null || jwt.isBlank()) throw new IllegalArgumentException("jwt is null or blank");
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(key)
                .build();
        return parser.parseClaimsJws(jwt).getBody();
    }

    public static String getJWTFromCookie(HttpServletRequest request) throws IOException {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static void bindJWTAsCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(JwtUtilConfig.HTTP_ONLY)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(expirePeriod)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static String refreshJWT(String jwt) {
        Claims claims = parseJWT(jwt);
        claims.setExpiration(new Date(System.currentTimeMillis() + expirePeriod));
        return generateJWT(claims);
    }

    public static boolean isEmailAddressValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches() || !isDomainResolvable(email)) {
            return false;
        }
        return true;
    }

    public static boolean isDomainResolvable(String email) {
        try {
            String domain = email.substring(email.indexOf("@") + 1);
            InetAddress.getByName(domain);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static long getRemainingSeconds(String jwt) {
        try {
            Claims claims = parseJWT(jwt);
            long now = System.currentTimeMillis();
            long exp = claims.getExpiration().getTime();
            return Math.max(0, (exp - now) / 1000);
        } catch (Exception e) {
            System.err.println("Failed to parse JWT in getRemainingSeconds: " + e.getMessage());
            return -1;
        }
    }

    public static long getExpirePeriod() {
        return expirePeriod;
    }
}

@Component
class JwtUtilConfig {
    @Value("${app.http-only}")
    public Boolean isHttpOnly;

    public static Boolean HTTP_ONLY;

    @PostConstruct
    private void init() {
        HTTP_ONLY = this.isHttpOnly;
    }
}