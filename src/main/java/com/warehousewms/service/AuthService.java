package com.warehousewms.service;

import com.warehousewms.model.User;
import com.warehousewms.repository.UserRepository;
import java.util.List;
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

    public boolean resetPassword(String username, String newPassword) throws Exception {
        return userRepo.updatePasswordByUsername(username, newPassword);
    }

    public boolean updatePassword(int userId, String newPassword) throws Exception {
        return userRepo.updatePasswordByUserId(userId, newPassword);
    }

    public List<User> listUsers() throws Exception {
        return userRepo.listUsers();
    }

    public void updateUser(User user) throws Exception {
        userRepo.updateUser(user);
    }

    public boolean deleteUser(int userId) throws Exception {
        return userRepo.deleteUser(userId);
    }

    @Override
    public void close() throws Exception {
        // No resources to close in this implementation, but method is here for future-proofing.
    }
}
