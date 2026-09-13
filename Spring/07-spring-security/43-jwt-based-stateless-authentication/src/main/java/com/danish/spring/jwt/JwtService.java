package com.danish.spring.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    // A real secret key, generated once when this bean is constructed. In a real
    // application this would be a fixed, securely-stored value (an environment
    // variable, a secrets manager) - if it changed on every restart, every previously
    // issued token would stop validating the moment the server restarted.
    private final SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

    private static final long EXPIRATION_MILLIS = 5_000; // deliberately short - see lesson demo

    public String generateToken(String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRATION_MILLIS))
                .signWith(key)
                .compact();
    }

    // Parses AND verifies the signature in one call - parseSignedClaims throws if the
    // signature does not match (tampered token) or if the token has expired. There is
    // no separate "read the claims" step that skips verification - you cannot get the
    // claims out without the signature check passing first.
    public Claims parseAndValidate(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
