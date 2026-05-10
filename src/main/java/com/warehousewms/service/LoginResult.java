package com.warehousewms.service;

import com.warehousewms.model.User;

public class LoginResult {
    private final boolean success;
    private final User user;
    private final String errorMessage;

    private LoginResult(boolean success, User user, String errorMessage) {
        this.success = success;
        this.user = user;
        this.errorMessage = errorMessage;
    }

    public static LoginResult ok(User user) {
        return new LoginResult(true, user, null);
    }

    public static LoginResult fail(String errorMessage) {
        return new LoginResult(false, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public User getUser() {
        return user;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
