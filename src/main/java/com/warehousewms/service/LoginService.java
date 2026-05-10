package com.warehousewms.service;

import com.warehousewms.model.User;
import com.warehousewms.repository.UserRepository;
import com.warehousewms.util.CredentialStorage;
import com.warehousewms.util.SessionContext;

import javax.sql.DataSource;

public class LoginService implements AutoCloseable {
    private final UserRepository userRepo;

    public LoginService(DataSource dataSource) {
        this.userRepo = new UserRepository(dataSource);
    }

    public LoginService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public LoginResult login(String username, String password, boolean rememberMe) {
        if (username == null || username.trim().isEmpty()) {
            return LoginResult.fail("Username is required.");
        }
        if (password == null || password.isEmpty()) {
            return LoginResult.fail("Password is required.");
        }

        try {
            User user = userRepo.authenticate(username.trim(), password);
            if (user == null) {
                return LoginResult.fail("Invalid username or password.");
            }

            SessionContext.setCurrentUser(user);

            if (rememberMe) {
                CredentialStorage.saveCredentials(username.trim(), password);
            } else {
                CredentialStorage.clearCredentials();
            }

            return LoginResult.ok(user);
        } catch (Exception e) {
            return LoginResult.fail("Login failed: " + e.getMessage());
        }
    }

    @Override
    public void close() {
    }
}
