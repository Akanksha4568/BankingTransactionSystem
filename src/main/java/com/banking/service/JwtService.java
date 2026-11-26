package com.banking.service;

import com.banking.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // Generate the signing key
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate JWT token
    public String generateToken(AppUser user) {

        Date currentDate = new Date();
        Date expiryDate = new Date(
                currentDate.getTime() + jwtExpiration
        );

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getUserId())
                .claim("role", user.getRole())
                .issuedAt(currentDate)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // Extract username from token
    public String extractUsername(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Validate token
    public boolean isTokenValid(String token, String username) {

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject().equals(username)
                && claims.getExpiration().after(new Date());
    }
}