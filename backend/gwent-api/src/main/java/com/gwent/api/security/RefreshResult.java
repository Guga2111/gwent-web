package com.gwent.api.security;

public record RefreshResult(String newTokenValue, String userId, String email) {
}
