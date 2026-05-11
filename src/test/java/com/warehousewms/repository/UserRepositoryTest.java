package com.warehousewms.repository;

import com.warehousewms.config.ConnectionPool;
import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {
    private DataSource ds;
    private UserRepository repo;

    @BeforeEach
    void setUp() {
        System.setProperty("wms.useSqlServer", "false");
        ds = new DatabaseManager().getDataSourceWithFallback();
        repo = new UserRepository(ds);
    }

    @AfterEach
    void tearDown() {
        ConnectionPool.shutdown();
        System.clearProperty("wms.useSqlServer");
    }

    @Test
    void authenticateWithValidCredentials() throws SQLException {
        // The default admin user has password "password"
        User user = repo.authenticate("admin", "password");
        assertNotNull(user, "Admin user should authenticate with correct password");
        assertEquals("admin", user.getUsername());
        assertEquals("Admin", user.getRole());
        assertEquals("System Administrator", user.getFullName());
    }

    @Test
    void authenticateWithWrongPassword() throws SQLException {
        User user = repo.authenticate("admin", "wrong");
        assertNull(user, "Should return null for wrong password");
    }

    @Test
    void authenticateWithNonExistentUser() throws SQLException {
        User user = repo.authenticate("nonexistent", "password");
        assertNull(user, "Should return null for non-existent user");
    }

    @Test
    void usernameExistsReturnsTrueForAdmin() throws SQLException {
        assertTrue(repo.usernameExists("admin"));
    }

    @Test
    void usernameExistsReturnsFalseForUnknown() throws SQLException {
        assertFalse(repo.usernameExists("unknownuser"));
    }

    @Test
    void insertAndListUser() throws SQLException {
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setPasswordHash("testpass123");
        newUser.setFullName("Test User");
        newUser.setRole("Operator");
        repo.insertUser(newUser);

        assertTrue(repo.usernameExists("testuser"));

        List<User> users = repo.listUsers();
        assertTrue(users.size() >= 2, "Should have at least admin + testuser");
        boolean found = users.stream().anyMatch(u -> "testuser".equals(u.getUsername()));
        assertTrue(found, "testuser should appear in the user list");
    }

    @Test
    void updateUser() throws SQLException {
        User newUser = new User();
        newUser.setUsername("updateme");
        newUser.setPasswordHash("pass");
        newUser.setFullName("Before Update");
        newUser.setRole("Operator");
        repo.insertUser(newUser);

        // Find the user to get the ID
        List<User> users = repo.listUsers();
        User inserted = users.stream().filter(u -> "updateme".equals(u.getUsername())).findFirst().orElse(null);
        assertNotNull(inserted);

        inserted.setFullName("After Update");
        inserted.setRole("Supervisor");
        repo.updateUser(inserted);

        User updated = repo.getUserById(inserted.getUserId());
        assertNotNull(updated);
        assertEquals("After Update", updated.getFullName());
        assertEquals("Supervisor", updated.getRole());
    }

    @Test
    void updatePasswordByUsername() throws SQLException {
        // Reset admin password, then authenticate with new password
        boolean updated = repo.updatePasswordByUsername("admin", "newpass123");
        assertTrue(updated);

        User user = repo.authenticate("admin", "newpass123");
        assertNotNull(user, "Should authenticate with new password");
    }

    @Test
    void deleteUser() throws SQLException {
        User newUser = new User();
        newUser.setUsername("deleteme");
        newUser.setPasswordHash("pass");
        newUser.setFullName("Delete Me");
        newUser.setRole("Operator");
        repo.insertUser(newUser);

        List<User> users = repo.listUsers();
        User inserted = users.stream().filter(u -> "deleteme".equals(u.getUsername())).findFirst().orElse(null);
        assertNotNull(inserted);

        boolean deleted = repo.deleteUser(inserted.getUserId());
        assertTrue(deleted);
        assertFalse(repo.usernameExists("deleteme"));
    }

    @Test
    void hashPasswordIsConsistent() {
        String hash1 = UserRepository.hashPassword("password");
        String hash2 = UserRepository.hashPassword("password");
        assertEquals(hash1, hash2, "Same input should produce same hash");
        assertEquals("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", hash1,
                "SHA-256 of 'password' should match known hash");
    }
}
