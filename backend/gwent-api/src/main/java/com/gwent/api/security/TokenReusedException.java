package com.gwent.api.security;

public class TokenReusedException extends RuntimeException {

    public TokenReusedException(String message) {
        super(message);
    }
}
