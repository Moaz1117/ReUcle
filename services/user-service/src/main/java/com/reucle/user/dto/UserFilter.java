package com.reucle.user.dto;

import com.reucle.user.domain.StaffRole;
import com.reucle.user.domain.UserStatus;

public record UserFilter(
    UserStatus status,
    StaffRole role,
    String emailContains
) {}
