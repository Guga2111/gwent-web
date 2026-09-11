package com.gwent.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

public class JwtTokenUtil {

    public static String createAccessToken(String email, String userId, String username,
                                           String secret, long expirationMs) {
        return JWT.create()
                .withSubject(email)
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationMs))
                .sign(Algorithm.HMAC512(secret));
    }
}
