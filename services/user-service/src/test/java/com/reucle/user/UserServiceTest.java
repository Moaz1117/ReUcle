package com.reucle.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.reucle.user.api.UserController;
import com.reucle.user.domain.AuditLog;
import com.reucle.user.domain.Customer;
import com.reucle.user.domain.Staff;
import com.reucle.user.domain.StaffRole;
import com.reucle.user.domain.User;
import com.reucle.user.domain.UserStatus;
import com.reucle.user.dto.LoginCredentials;
import com.reucle.user.dto.RegistrationData;
import com.reucle.user.dto.Token;
import com.reucle.user.dto.UserFilter;
import com.reucle.user.exceptions.AccountNotActiveException;
import com.reucle.user.exceptions.DuplicateEmailException;
import com.reucle.user.exceptions.InvalidCredentialsException;
import com.reucle.user.exceptions.InvalidUserStateTransitionException;
import com.reucle.user.exceptions.UserNotFoundException;
import com.reucle.user.repository.IAuditRepository;
import com.reucle.user.repository.IUserRepository;
import com.reucle.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IAuditRepository auditRepository;

    private UserService userService;
    private UserController userController;

    private Customer alice;
    private LoginCredentials creds;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, auditRepository);
        userController = new UserController(userService);

        alice = new Customer(
            "U-1001",
            "Alice Hassan",
            "alice.hassan@example.com",
            "S3cret!pw",
            UserStatus.ACTIVE,
            "12 Nile Corniche, Maadi, Cairo",
            "+20 100 123 4567"
        );
        creds = new LoginCredentials("alice.hassan@example.com", "S3cret!pw");
    }

    @Test
    void login_validCredentialsAndActiveUser_returnsTokenAndLogsSuccess() {
        when(userRepository.findByEmail("alice.hassan@example.com")).thenReturn(alice);

        Token token1 = userController.handleLogin(creds);

        assertNotNull(token1);
        assertEquals("U-1001", token1.userId());
        assertTrue(token1.expiresAt().isAfter(Instant.now()));

        InOrder order = inOrder(userRepository, auditRepository);
        order.verify(userRepository).findByEmail("alice.hassan@example.com");
        order.verify(auditRepository).logAction("U-1001", "LOGIN_SUCCESS");
    }

    @Test
    void login_userNotFound_throwsInvalidCredentialsException() {
        when(userRepository.findByEmail("alice.hassan@example.com")).thenReturn(null);
        assertThrows(InvalidCredentialsException.class, () -> userService.login(creds));
        verifyNoInteractions(auditRepository);
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentialsAndLogsFailure() {
        when(userRepository.findByEmail("alice.hassan@example.com")).thenReturn(alice);
        LoginCredentials badCreds = new LoginCredentials("alice.hassan@example.com", "Wrong!pw");

        assertThrows(InvalidCredentialsException.class, () -> userService.login(badCreds));
        verify(auditRepository).logAction("U-1001", "LOGIN_FAILED");
    }

    @Test
    void login_whenNotActive_throwsAccountNotActiveException() {
        alice.suspend();
        when(userRepository.findByEmail("alice.hassan@example.com")).thenReturn(alice);

        assertThrows(AccountNotActiveException.class, () -> userService.login(creds));
        verify(auditRepository, never()).logAction(any(), any());
    }

    @Test
    void register_andCrudQueries_workAsExpected() {
        RegistrationData reg = new RegistrationData("Omar", "omar@example.com", "Pass1", "Alex", "+20111");
        when(userRepository.findByEmail("omar@example.com")).thenReturn(null);

        userService.register(reg);
        verify(userRepository).save(any(Customer.class));
        verify(auditRepository).logAction(anyString(), eq("USER_REGISTERED"));

        when(userRepository.findByEmail("alice.hassan@example.com")).thenReturn(alice);
        assertThrows(DuplicateEmailException.class, () ->
            userService.register(new RegistrationData("Alice", "alice.hassan@example.com", "pw", "addr", "ph"))
        );

        userService.resetPassword("alice.hassan@example.com");
        verify(auditRepository).logAction("U-1001", "PASSWORD_RESET_REQUESTED");
        assertThrows(UserNotFoundException.class, () -> userService.resetPassword("none@example.com"));

        when(userRepository.findById("U-1001")).thenReturn(alice);
        assertEquals("Alice Hassan", userService.getUser("U-1001").getName());
        assertThrows(UserNotFoundException.class, () -> userService.getUser("U-9999"));

        Staff admin = new Staff("S-1", "Admin", "admin@reucle.com", "Admin123", UserStatus.ACTIVE, StaffRole.ADMIN);
        when(userRepository.findAll()).thenReturn(List.of(alice, admin));
        List<User> filtered = userService.listUsers(new UserFilter(UserStatus.ACTIVE, StaffRole.ADMIN, "admin"));
        assertEquals(1, filtered.size());
        assertEquals("S-1", filtered.get(0).getUserId());
    }

    @Test
    void stateTransitions_validAndInvalid() {
        Customer pendingUser = new Customer(
            "U-1002", "Pending User", "pending@example.com",
            "Pass123", UserStatus.PENDING_VERIFICATION, "Cairo", "+20 100 000 0000"
        );
        pendingUser.updateProfile("New Cairo", "+20 100 111 2222");
        assertEquals("New Cairo", pendingUser.getCollectionAddress());

        assertThrows(InvalidUserStateTransitionException.class, pendingUser::suspend);
        assertThrows(InvalidUserStateTransitionException.class, pendingUser::reinstate);

        pendingUser.verifyEmail();
        assertEquals(UserStatus.ACTIVE, pendingUser.getStatus());
        assertThrows(InvalidUserStateTransitionException.class, pendingUser::verifyEmail);
        assertThrows(InvalidUserStateTransitionException.class, pendingUser::reinstate);

        pendingUser.suspend();
        assertEquals(UserStatus.SUSPENDED, pendingUser.getStatus());
        assertThrows(InvalidUserStateTransitionException.class, pendingUser::suspend);
        assertThrows(InvalidUserStateTransitionException.class, pendingUser::verifyEmail);

        pendingUser.reinstate();
        assertEquals(UserStatus.ACTIVE, pendingUser.getStatus());

        pendingUser.markDeleted();
        assertEquals(UserStatus.DELETED, pendingUser.getStatus());
        assertThrows(InvalidUserStateTransitionException.class, pendingUser::suspend);
        assertThrows(InvalidUserStateTransitionException.class, pendingUser::reinstate);
        assertThrows(InvalidUserStateTransitionException.class, pendingUser::verifyEmail);
        assertThrows(InvalidUserStateTransitionException.class, pendingUser::markDeleted);

        AuditLog log1 = new AuditLog("LOG-50001", "U-1001", "LOGIN_SUCCESS", Instant.now());
        assertEquals("LOG-50001", log1.getLogId());
    }
}
