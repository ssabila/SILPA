package ppk.silpa.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    // Generate JWT token
    public String generateToken(Authentication authentication){
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(key()) // Gunakan metode key() yang sudah diperbaiki
                .compact();
    }

    private Key key(){
        // Gunakan getBytes() untuk secret yang berupa string biasa (bukan Base64)
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Get username from JWT token
    public String getUsername(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // Validate JWT token
    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key()) // Gunakan metode key() yang sudah diperbaiki
                    .build()
                    .parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) { // Tangkap exception yang lebih spesifik
            // Log exception di sini jika perlu (misal: logger.error("Validasi JWT gagal: {}", ex.getMessage());)
            System.err.println("Validasi JWT gagal: " + ex.getMessage()); // Contoh logging ke console
            return false;
        }
    }
}