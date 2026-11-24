package com.reucle.user.repository;

import java.util.List;
import com.reucle.user.domain.User;

public interface IUserRepository {
    void save(User user);
    User findById(String id);
    User findByEmail(String email);
    List<User> findAll();
}
