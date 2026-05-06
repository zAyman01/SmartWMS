package com.warehousewms.service;

import com.warehousewms.model.User;
import com.warehousewms.repository.UserRepository;
import javax.sql.DataSource;

public class AuthService {
    private final UserRepository userRepo;

    public AuthService(DataSource dataSource) {
        this.userRepo = new UserRepository(dataSource);
    }

    public User login(String username, String password) throws Exception {
        return userRepo.authenticate(username, password);
    }
}

