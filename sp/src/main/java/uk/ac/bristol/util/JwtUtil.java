package uk.ac.bristol.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

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

        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401, \"message\":\"Missing or empty token\"}");
        return null;
    }

    public static void bindJWTAsCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
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
            InetAddress.getByName(domain); // 尝试解析域名
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
