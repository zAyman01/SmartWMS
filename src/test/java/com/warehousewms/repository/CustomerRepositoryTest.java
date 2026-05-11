package com.warehousewms.repository;

import com.warehousewms.config.ConnectionPool;
import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.Customer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerRepositoryTest {
    private DataSource ds;
    private CustomerRepository repo;

    @BeforeEach
    void setUp() {
        System.setProperty("wms.useSqlServer", "false");
        ds = new DatabaseManager().getDataSourceWithFallback();
        repo = new CustomerRepository(ds);
    }

    @AfterEach
    void tearDown() {
        ConnectionPool.shutdown();
        System.clearProperty("wms.useSqlServer");
    }

    @Test
    void insertAndListAll() throws SQLException {
        Customer c = new Customer();
        c.setName("Test Customer");
        c.setContactName("Contact Person");
        c.setEmail("cust@example.com");
        c.setPhone("555-0200");
        repo.insert(c);

        List<Customer> all = repo.listAll();
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(x -> "Test Customer".equals(x.getName())));
    }

    @Test
    void findById() throws SQLException {
        Customer c = new Customer();
        c.setName("Findable Customer");
        repo.insert(c);

        Customer found = repo.listAll().stream().filter(x -> "Findable Customer".equals(x.getName())).findFirst().orElse(null);
        assertNotNull(found);

        Customer byId = repo.findById(found.getCustomerId());
        assertNotNull(byId);
        assertEquals("Findable Customer", byId.getName());
    }

    @Test
    void updateCustomer() throws SQLException {
        Customer c = new Customer();
        c.setName("Original");
        repo.insert(c);

        Customer found = repo.listAll().stream().filter(x -> "Original".equals(x.getName())).findFirst().orElse(null);
        assertNotNull(found);
        found.setName("Updated");
        found.setPhone("555-9999");
        repo.update(found);

        Customer updated = repo.findById(found.getCustomerId());
        assertEquals("Updated", updated.getName());
        assertEquals("555-9999", updated.getPhone());
    }

    @Test
    void deleteCustomer() throws SQLException {
        Customer c = new Customer();
        c.setName("To Remove");
        repo.insert(c);

        Customer found = repo.listAll().stream().filter(x -> "To Remove".equals(x.getName())).findFirst().orElse(null);
        assertNotNull(found);

        assertTrue(repo.delete(found.getCustomerId()));
        assertNull(repo.findById(found.getCustomerId()));
    }
}
