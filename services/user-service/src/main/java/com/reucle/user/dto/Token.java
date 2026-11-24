package com.reucle.user.dto;

import java.time.Instant;

public record Token(
    String value,
    String userId,
    Instant expiresAt
) {}
