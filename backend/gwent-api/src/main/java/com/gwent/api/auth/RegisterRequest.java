package com.gwent.api.auth;

public record RegisterRequest(String email, String username, String password) {}