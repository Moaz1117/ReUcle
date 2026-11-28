package com.reucle.user.api;

import java.util.List;
import com.reucle.user.domain.User;
import com.reucle.user.dto.LoginCredentials;
import com.reucle.user.dto.RegistrationData;
import com.reucle.user.dto.Token;
import com.reucle.user.dto.UserFilter;

public interface IUser {
    void register(RegistrationData data);
    Token login(LoginCredentials credentials);
    void resetPassword(String email);
    User getUser(String id);
    List<User> listUsers(UserFilter filter);
}
