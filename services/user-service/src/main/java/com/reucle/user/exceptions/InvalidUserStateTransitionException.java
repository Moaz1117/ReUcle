package com.reucle.user.exceptions;

public class InvalidUserStateTransitionException extends RuntimeException {
    public InvalidUserStateTransitionException(String message) { super(message); }
}
