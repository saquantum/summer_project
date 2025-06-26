package org.example.mail.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.example.mail.Result.ParsedTokenResult;

import java.rmi.server.UID;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

public class JwtUtil {
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    //for email
    public static String generateToken(String email, String tokenId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("action", "unsubscribe")
                .claim("tokenId", tokenId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24)) // 1 天有效
                .signWith(key)
                .compact();
    }

    //for whatsapp
    public static String createToken(String phoneNumber) {
        return Jwts.builder()
                .setSubject(phoneNumber)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+ 1000L * 60 * 60 * 24)) // 1 天有效
                .signWith(key)
                .compact();
    }

    //for email
    public static ParsedTokenResult parseToken(String token) {

        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();

        if (!"unsubscribe".equals(claims.get("action"))) {
            throw new SecurityException("fake token");
        }

        String email = claims.getSubject();
        String tokenId = claims.get("tokenId", String.class);

        return new ParsedTokenResult(email, tokenId);
    }

    //for whatsapp
    public static String parseTokenWhatsApp(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
