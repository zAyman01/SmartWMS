package com.warehousewms.repository;

import com.warehousewms.config.ConnectionPool;
import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupplierRepositoryTest {
    private DataSource ds;
    private SupplierRepository repo;

    @BeforeEach
    void setUp() {
        System.setProperty("wms.useSqlServer", "false");
        ds = new DatabaseManager().getDataSourceWithFallback();
        repo = new SupplierRepository(ds);
    }

    @AfterEach
    void tearDown() {
        ConnectionPool.shutdown();
        System.clearProperty("wms.useSqlServer");
    }

    @Test
    void insertAndListAll() throws SQLException {
        Supplier s = new Supplier();
        s.setName("Acme Corp");
        s.setContactName("John Doe");
        s.setEmail("john@acme.com");
        s.setPhone("555-0100");
        repo.insert(s);

        List<Supplier> all = repo.listAll();
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(x -> "Acme Corp".equals(x.getName())));
    }

    @Test
    void findById() throws SQLException {
        Supplier s = new Supplier();
        s.setName("FindMe Inc");
        s.setContactName("Jane");
        repo.insert(s);

        Supplier found = repo.listAll().stream().filter(x -> "FindMe Inc".equals(x.getName())).findFirst().orElse(null);
        assertNotNull(found);

        Supplier byId = repo.findById(found.getSupplierId());
        assertNotNull(byId);
        assertEquals("FindMe Inc", byId.getName());
        assertEquals("Jane", byId.getContactName());
    }

    @Test
    void updateSupplier() throws SQLException {
        Supplier s = new Supplier();
        s.setName("Old Name");
        repo.insert(s);

        Supplier found = repo.listAll().stream().filter(x -> "Old Name".equals(x.getName())).findFirst().orElse(null);
        assertNotNull(found);
        found.setName("New Name");
        found.setEmail("new@example.com");
        repo.update(found);

        Supplier updated = repo.findById(found.getSupplierId());
        assertEquals("New Name", updated.getName());
        assertEquals("new@example.com", updated.getEmail());
    }

    @Test
    void deleteSupplier() throws SQLException {
        Supplier s = new Supplier();
        s.setName("Delete Me");
        repo.insert(s);

        Supplier found = repo.listAll().stream().filter(x -> "Delete Me".equals(x.getName())).findFirst().orElse(null);
        assertNotNull(found);

        assertTrue(repo.delete(found.getSupplierId()));
        assertNull(repo.findById(found.getSupplierId()));
    }
}
