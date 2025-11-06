package gustavo.com.cryptoaiinvestor.Config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key secretKey;
    private final long expirationTime;

    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expirationTime) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationTime = expirationTime;
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            boolean isTokenExpired = claims.getExpiration().before(new Date());
            return (username.equals(userDetails.getUsername()) && !isTokenExpired);
        } catch (ExpiredJwtException e) {
            System.out.println("JWT expired: " + e.getMessage());
        } catch (MalformedJwtException | UnsupportedJwtException e) {
            System.out.println("Invalid JWT: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("JWT validation error: " + e.getMessage());
        }
        return false;
    }


    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
