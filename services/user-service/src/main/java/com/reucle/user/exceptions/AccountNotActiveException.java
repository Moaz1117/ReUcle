package com.reucle.user.exceptions;

public class AccountNotActiveException extends RuntimeException {
    public AccountNotActiveException(String message) { super(message); }
}
