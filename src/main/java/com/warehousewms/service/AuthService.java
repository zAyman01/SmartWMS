package com.warehousewms.service;

import com.warehousewms.model.User;
import com.warehousewms.repository.UserRepository;
import javax.sql.DataSource;

public class AuthService implements AutoCloseable {
    private final UserRepository userRepo;

    public AuthService(DataSource dataSource) {
        this.userRepo = new UserRepository(dataSource);
    }

    public User login(String username, String password) throws Exception {
        return userRepo.authenticate(username, password);
    }

    public boolean usernameExists(String username) throws Exception {
        return userRepo.usernameExists(username);
    }

    public void register(User user) throws Exception {
        userRepo.insertUser(user);
    }

    @Override
    public void close() throws Exception {
        // No resources to close in this implementation, but method is here for future-proofing.
    }
}
