package com.warehousewms.service;

import com.warehousewms.config.ConnectionPool;
import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.User;
import com.warehousewms.repository.UserRepository;
import com.warehousewms.util.SessionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

class LoginServiceTest {
    private DataSource ds;
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        System.setProperty("wms.useSqlServer", "false");
        ds = new DatabaseManager().getDataSourceWithFallback();
        loginService = new LoginService(ds);
        SessionContext.clear();
    }

    @AfterEach
    void tearDown() {
        SessionContext.clear();
        ConnectionPool.shutdown();
        System.clearProperty("wms.useSqlServer");
    }

    @Test
    void loginSuccess() {
        LoginResult result = loginService.login("admin", "admin123", false);
        assertTrue(result.isSuccess());
        assertNotNull(result.getUser());
        assertEquals("admin", result.getUser().getUsername());
        assertNull(result.getErrorMessage());

        assertNotNull(SessionContext.getCurrentUser());
        assertEquals("admin", SessionContext.getCurrentUser().getUsername());
    }

    @Test
    void loginFailsWithWrongPassword() {
        LoginResult result = loginService.login("admin", "wrongpassword", false);
        assertFalse(result.isSuccess());
        assertNull(result.getUser());
        assertEquals("Invalid username or password.", result.getErrorMessage());
    }

    @Test
    void loginFailsWithEmptyUsername() {
        LoginResult result = loginService.login("", "password", false);
        assertFalse(result.isSuccess());
        assertEquals("Username is required.", result.getErrorMessage());
    }

    @Test
    void loginFailsWithNullUsername() {
        LoginResult result = loginService.login(null, "password", false);
        assertFalse(result.isSuccess());
        assertEquals("Username is required.", result.getErrorMessage());
    }

    @Test
    void loginFailsWithEmptyPassword() {
        LoginResult result = loginService.login("admin", "", false);
        assertFalse(result.isSuccess());
        assertEquals("Password is required.", result.getErrorMessage());
    }

    @Test
    void loginFailsWithNullPassword() {
        LoginResult result = loginService.login("admin", null, false);
        assertFalse(result.isSuccess());
        assertEquals("Password is required.", result.getErrorMessage());
    }

    @Test
    void loginFailsWithNonExistentUser() {
        LoginResult result = loginService.login("nobody", "password", false);
        assertFalse(result.isSuccess());
        assertEquals("Invalid username or password.", result.getErrorMessage());
    }

    @Test
    void loginResultOkFactory() {
        User u = new User();
        u.setUsername("test");
        LoginResult result = LoginResult.ok(u);
        assertTrue(result.isSuccess());
        assertNotNull(result.getUser());
        assertNull(result.getErrorMessage());
    }

    @Test
    void loginResultFailFactory() {
        LoginResult result = LoginResult.fail("error msg");
        assertFalse(result.isSuccess());
        assertNull(result.getUser());
        assertEquals("error msg", result.getErrorMessage());
    }
}
