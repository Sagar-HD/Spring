package com.sagar.jwtsecurity.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    // Base64 encoded secret.
    // In production, keep this in environment variables / secret manager.
    private static final String SECRET =
            "c2VjcmV0LWtleS1mb3Itand0LWF1dGhlbnRpY2F0aW9uLXNob3VsZC1iZS1sb25n";

    private static final long EXPIRATION_TIME =
            1000 * 60 * 60; // 1 hour


    // =========================================================
    // 1. Generate JWT from Authentication
    // =========================================================

    public String generateToken(Authentication authentication) {

        String username = authentication.getName();

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Date now = new Date();

        Date expiry = new Date(
                now.getTime() + EXPIRATION_TIME
        );

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }


    // =========================================================
    // 2. Extract username / subject
    // =========================================================

    public String extractUsername(String token) {

        return extractAllClaims(token)
                .getSubject();
    }


    // =========================================================
    // 3. Extract roles
    // =========================================================

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {

        return extractAllClaims(token)
                .get("roles", List.class);
    }


    // =========================================================
    // 4. Extract all claims
    // =========================================================

    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    // =========================================================
    // 5. Validate JWT
    // =========================================================

    public boolean isTokenValid(String token) {

        try {

            extractAllClaims(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }


    // =========================================================
    // 6. Validate JWT against a particular user
    // =========================================================

    public boolean isTokenValid(
            String token,
            String username
    ) {

        try {

            String tokenUsername = extractUsername(token);

            return tokenUsername.equals(username)
                    && !isTokenExpired(token);

        } catch (Exception e) {

            return false;
        }
    }


    // =========================================================
    // 7. Check expiration
    // =========================================================

    public boolean isTokenExpired(String token) {

        Date expiration =
                extractAllClaims(token).getExpiration();

        return expiration.before(new Date());
    }


    // =========================================================
    // 8. Extract token from Authorization header
    // =========================================================

    public String extractToken(String authorizationHeader) {

        if (authorizationHeader == null) {
            return null;
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        return authorizationHeader.substring(7);
    }


    // =========================================================
    // 9. Create signing key
    // =========================================================

    private SecretKey getSigningKey() {

        byte[] keyBytes =
                Decoders.BASE64.decode(SECRET);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}