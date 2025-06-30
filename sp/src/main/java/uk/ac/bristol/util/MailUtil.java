package uk.ac.bristol.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.InetAddress;
import java.security.Key;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailUtil {

    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public static String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("action", "unsubscribe")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24)) // 1 天有效
                .signWith(key)
                .compact();
    }

    public static String parseToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();

        if (!"unsubscribe".equals(claims.get("action"))) {
            throw new SecurityException("fake token");
        }

        String email = claims.getSubject();

        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // 返回 email
    }

    public ResponseEntity<String> checkEmail(@RequestParam String email) {
        String emailRegex = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            return ResponseEntity.badRequest().body("Invalid email!");
        }

        if (!isDomainResolvable(email)) {
            return ResponseEntity.badRequest().body("Invalid email!");
        }
        return ResponseEntity.ok("Correct email!");
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
