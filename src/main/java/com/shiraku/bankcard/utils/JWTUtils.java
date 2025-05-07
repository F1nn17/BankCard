package com.shiraku.bankcard.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.shiraku.bankcard.model.Role;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JWTUtils {
    private static final String SECRET_KEY = "secretKey12345";
    private static final long EXPIRATION_TIME = 86400000;

    public String generateToken(UUID id, String email, Role role) {
        return JWT.create()
                .withSubject(id.toString())
                .withClaim("email", email)
                .withClaim("role", role.name())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    public String extractUsername(String token) {
        return extractClaim(token, "sub");
    }

    public String extractEmail(String token) {
        return extractClaim(token, "email");
    }

    public String extractRole(String token) {
        return extractClaim(token, "role");
    }

    private String extractClaim(String token, String claimKey) {
        DecodedJWT decodedJWT = decodeToken(token);
        return decodedJWT.getClaim(claimKey).asString();
    }

    private DecodedJWT decodeToken(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .build();
        return verifier.verify(token);
    }

    public boolean isTokenValid(String token) {
        return (!isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        DecodedJWT decodedJWT = decodeToken(token);
        return decodedJWT.getExpiresAt().before(new Date());
    }
}