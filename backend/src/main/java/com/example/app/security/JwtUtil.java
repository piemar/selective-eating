package com.example.app.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${app.jwt.access-secret}")
    private String accessSecret;

    @Value("${app.jwt.refresh-secret}")
    private String refreshSecret;

    @Value("${app.jwt.access-ttl-seconds}")
    private long accessTokenTtl;

    @Value("${app.jwt.refresh-ttl-seconds}")
    private long refreshTokenTtl;

    // Generate access token
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), accessTokenTtl * 1000, getAccessTokenKey());
    }

    // Generate refresh token
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshTokenTtl * 1000, getRefreshTokenKey());
    }

    // Generate token with custom claims
    public String generateAccessToken(String username, Map<String, Object> claims) {
        return createToken(claims, username, accessTokenTtl * 1000, getAccessTokenKey());
    }

    // Create token
    private String createToken(Map<String, Object> claims, String subject, long ttlMillis, SecretKey key) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttlMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract username from access token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject, getAccessTokenKey());
    }

    // Extract username from refresh token
    public String extractUsernameFromRefreshToken(String token) {
        return extractClaim(token, Claims::getSubject, getRefreshTokenKey());
    }

    // Extract expiration date
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration, getAccessTokenKey());
    }

    // Extract a specific claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, SecretKey key) {
        final Claims claims = extractAllClaims(token, key);
        return claimsResolver.apply(claims);
    }

    // Extract all claims
    private Claims extractAllClaims(String token, SecretKey key) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Check if token is expired
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    // Validate access token
    public Boolean validateAccessToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            return false;
        }
    }

    // Validate refresh token
    public Boolean validateRefreshToken(String token, String username) {
        try {
            final String tokenUsername = extractUsernameFromRefreshToken(token);
            final Claims claims = extractAllClaims(token, getRefreshTokenKey());
            final String tokenType = (String) claims.get("type");
            
            return (tokenUsername.equals(username) 
                    && "refresh".equals(tokenType)
                    && !token.isEmpty() 
                    && !isRefreshTokenExpired(token));
        } catch (JwtException e) {
            return false;
        }
    }

    // Check if refresh token is expired
    public Boolean isRefreshTokenExpired(String token) {
        try {
            final Claims claims = extractAllClaims(token, getRefreshTokenKey());
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    // Get access token signing key
    private SecretKey getAccessTokenKey() {
        return Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Get refresh token signing key
    private SecretKey getRefreshTokenKey() {
        return Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Extract token type
    public String extractTokenType(String token) {
        try {
            final Claims claims = extractAllClaims(token, getRefreshTokenKey());
            return (String) claims.get("type");
        } catch (JwtException e) {
            return "access"; // Default to access token
        }
    }

    // Parse token safely (for validation without throwing exceptions)
    public boolean canTokenBeTrusted(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getAccessTokenKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // Get token TTL in seconds
    public long getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public long getRefreshTokenTtl() {
        return refreshTokenTtl;
    }
}
