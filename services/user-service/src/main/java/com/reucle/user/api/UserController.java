package com.reucle.user.api;

import com.reucle.user.dto.LoginCredentials;
import com.reucle.user.dto.Token;

public class UserController {
    private final IUser userApi;

    public UserController(IUser userApi) {
        this.userApi = userApi;
    }

    public Token handleLogin(LoginCredentials creds) {
        return userApi.login(creds);
    }
}
