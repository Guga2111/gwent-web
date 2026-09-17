package com.gwent.api.auth;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 3, max = 20) @Pattern(regexp = "^[a-zA-Z0-9_-]+$") String username,
        @NotBlank @Size(min = 8, max = 72) String password
) {}
