package com.reucle.user.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import com.reucle.user.api.IUser;
import com.reucle.user.domain.Customer;
import com.reucle.user.domain.Staff;
import com.reucle.user.domain.User;
import com.reucle.user.domain.UserStatus;
import com.reucle.user.dto.LoginCredentials;
import com.reucle.user.dto.RegistrationData;
import com.reucle.user.dto.Token;
import com.reucle.user.dto.UserFilter;
import com.reucle.user.exceptions.AccountNotActiveException;
import com.reucle.user.exceptions.DuplicateEmailException;
import com.reucle.user.exceptions.InvalidCredentialsException;
import com.reucle.user.exceptions.UserNotFoundException;
import com.reucle.user.repository.IAuditRepository;
import com.reucle.user.repository.IUserRepository;

public class UserService implements IUser {
    private final IUserRepository userRepository;
    private final IAuditRepository auditRepository;

    public UserService(IUserRepository userRepository, IAuditRepository auditRepository) {
        this.userRepository = userRepository;
        this.auditRepository = auditRepository;
    }

    @Override
    public void register(RegistrationData data) {
        if (userRepository.findByEmail(data.email()) != null) {
            throw new DuplicateEmailException("Email already exists: " + data.email());
        }
        Customer customer = new Customer(
            "U-" + UUID.randomUUID().toString().substring(0, 8),
            data.name(),
            data.email(),
            data.password(),
            UserStatus.PENDING_VERIFICATION,
            data.collectionAddress(),
            data.phone()
        );
        userRepository.save(customer);
        auditRepository.logAction(customer.getUserId(), "USER_REGISTERED");
    }

    @Override
    public Token login(LoginCredentials credentials) {
        User user = userRepository.findByEmail(credentials.email());
        if (user == null) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("Account status is: " + user.getStatus());
        }
        if (!authenticate(user, credentials.password())) {
            auditRepository.logAction(user.getUserId(), "LOGIN_FAILED");
            throw new InvalidCredentialsException("Invalid email or password");
        }
        auditRepository.logAction(user.getUserId(), "LOGIN_SUCCESS");
        return issueToken(user);
    }

    public boolean authenticate() {
        return true;
    }

    private boolean authenticate(User user, String rawPassword) {
        return user.authenticate(rawPassword);
    }

    private Token issueToken(User user) {
        return new Token(
            "jwt-" + UUID.randomUUID(),
            user.getUserId(),
            Instant.now().plus(2, ChronoUnit.HOURS)
        );
    }

    @Override
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User not found for email: " + email);
        }
        auditRepository.logAction(user.getUserId(), "PASSWORD_RESET_REQUESTED");
    }

    @Override
    public User getUser(String id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new UserNotFoundException("User not found: " + id);
        }
        return user;
    }

    @Override
    public List<User> listUsers(UserFilter filter) {
        return userRepository.findAll().stream()
                .filter(u -> filter == null || filter.status() == null || u.getStatus() == filter.status())
                .filter(u -> filter == null || filter.role() == null || (u instanceof Staff s && s.getRole() == filter.role()))
                .filter(u -> filter == null || filter.emailContains() == null || u.getEmail().contains(filter.emailContains()))
                .toList();
    }
}
