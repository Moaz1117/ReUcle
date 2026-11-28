package com.reucle.user.domain;

public class Staff extends User {
    private StaffRole role;

    public Staff(String userId, String name, String email, String passwordHash,
                 UserStatus status, StaffRole role) {
        super(userId, name, email, passwordHash, status);
        this.role = role;
    }

    public StaffRole getRole() { return role; }
}
