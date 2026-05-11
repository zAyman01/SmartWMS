package com.warehousewms.util;

import com.warehousewms.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionContextTest {

    @AfterEach
    void tearDown() {
        SessionContext.clear();
    }

    @Test
    void setAndGetCurrentUser() {
        User user = new User();
        user.setUsername("test");
        user.setRole("Operator");
        SessionContext.setCurrentUser(user);

        assertNotNull(SessionContext.getCurrentUser());
        assertEquals("test", SessionContext.getCurrentUser().getUsername());
    }

    @Test
    void clearRemovesUser() {
        User user = new User();
        user.setUsername("test");
        SessionContext.setCurrentUser(user);
        SessionContext.clear();

        assertNull(SessionContext.getCurrentUser());
    }

    @Test
    void isAdminReturnsTrueForAdmin() {
        User admin = new User();
        admin.setRole("Admin");
        SessionContext.setCurrentUser(admin);
        assertTrue(SessionContext.isAdmin());
    }

    @Test
    void isAdminReturnsFalseForNonAdmin() {
        User user = new User();
        user.setRole("Operator");
        SessionContext.setCurrentUser(user);
        assertFalse(SessionContext.isAdmin());
    }

    @Test
    void isAdminReturnsFalseWhenNoUser() {
        assertFalse(SessionContext.isAdmin());
    }

    @Test
    void isAdminIsCaseInsensitive() {
        User user = new User();
        user.setRole("admin");
        SessionContext.setCurrentUser(user);
        assertTrue(SessionContext.isAdmin());
    }
}
