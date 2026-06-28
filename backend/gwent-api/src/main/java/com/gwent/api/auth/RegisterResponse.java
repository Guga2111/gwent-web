package com.gwent.api.auth;

import java.util.UUID;

public record RegisterResponse(UUID id, String email, String username) {}