package com.reucle.user.domain;

import com.reucle.user.exceptions.InvalidUserStateTransitionException;

public abstract class User {
    protected String userId;
    protected String name;
    protected String email;
    protected String passwordHash;
    protected UserStatus status;

    protected User(String userId, String name, String email, String passwordHash, UserStatus status) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
    }

    public boolean authenticate(String pwd) {
        return pwd != null && this.passwordHash.equals(pwd);
    }

    public void verifyEmail() {
        if (this.status != UserStatus.PENDING_VERIFICATION) {
            throw new InvalidUserStateTransitionException(
                "verifyEmail() only allowed from PENDING_VERIFICATION; current: " + this.status
            );
        }
        changeStatus(UserStatus.ACTIVE);
    }

    public void suspend() {
        if (this.status != UserStatus.ACTIVE) {
            throw new InvalidUserStateTransitionException(
                "suspend() only allowed from ACTIVE; current: " + this.status
            );
        }
        changeStatus(UserStatus.SUSPENDED);
    }

    public void reinstate() {
        if (this.status != UserStatus.SUSPENDED) {
            throw new InvalidUserStateTransitionException(
                "reinstate() only allowed from SUSPENDED; current: " + this.status
            );
        }
        changeStatus(UserStatus.ACTIVE);
    }

    public void markDeleted() {
        if (this.status == UserStatus.DELETED) {
            throw new InvalidUserStateTransitionException("User is already DELETED");
        }
        changeStatus(UserStatus.DELETED);
    }

    protected void changeStatus(UserStatus target) {
        this.status = target;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserStatus getStatus() { return status; }
}
