package com.warehousewms.repository;

import com.warehousewms.config.ConnectionPool;
import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductRepositoryTest {
    private DataSource ds;
    private ProductRepository repo;

    @BeforeEach
    void setUp() {
        System.setProperty("wms.useSqlServer", "false");
        ds = new DatabaseManager().getDataSourceWithFallback();
        repo = new ProductRepository(ds);
    }

    @AfterEach
    void tearDown() {
        ConnectionPool.shutdown();
        System.clearProperty("wms.useSqlServer");
    }

    @Test
    void insertAndFindById() throws SQLException {
        Product p = new Product();
        p.setSku("TEST-001");
        p.setName("Test Product");
        p.setUnitWeightKg(1.5);
        p.setUnitVolumeM3(0.001);
        p.setActive(true);
        repo.insert(p);

        // Find by listing all and getting the first match
        List<Product> all = repo.listAll();
        Product found = all.stream().filter(x -> "TEST-001".equals(x.getSku())).findFirst().orElse(null);
        assertNotNull(found);
        assertEquals("Test Product", found.getName());
        assertEquals(1.5, found.getUnitWeightKg(), 0.001);
        assertTrue(found.isActive());

        // Find by ID
        Product byId = repo.findById(found.getProductId());
        assertNotNull(byId);
        assertEquals("TEST-001", byId.getSku());
    }

    @Test
    void skuExists() throws SQLException {
        Product p = new Product();
        p.setSku("UNIQUE-SKU");
        p.setName("Unique");
        repo.insert(p);

        assertTrue(repo.skuExists("UNIQUE-SKU"));
        assertFalse(repo.skuExists("NONEXISTENT"));
    }

    @Test
    void listActiveFiltersInactive() throws SQLException {
        Product active = new Product();
        active.setSku("ACTIVE-1");
        active.setName("Active Product");
        active.setActive(true);
        repo.insert(active);

        Product inactive = new Product();
        inactive.setSku("INACTIVE-1");
        inactive.setName("Inactive Product");
        inactive.setActive(false);
        repo.insert(inactive);

        List<Product> activeList = repo.listActive();
        assertTrue(activeList.stream().anyMatch(p -> "ACTIVE-1".equals(p.getSku())));
        assertFalse(activeList.stream().anyMatch(p -> "INACTIVE-1".equals(p.getSku())));
    }

    @Test
    void updateProduct() throws SQLException {
        Product p = new Product();
        p.setSku("UPD-001");
        p.setName("Before");
        p.setUnitWeightKg(1.0);
        repo.insert(p);

        Product found = repo.listAll().stream().filter(x -> "UPD-001".equals(x.getSku())).findFirst().orElse(null);
        assertNotNull(found);
        found.setName("After");
        found.setUnitWeightKg(2.5);
        repo.update(found);

        Product updated = repo.findById(found.getProductId());
        assertEquals("After", updated.getName());
        assertEquals(2.5, updated.getUnitWeightKg(), 0.001);
    }

    @Test
    void deleteProduct() throws SQLException {
        Product p = new Product();
        p.setSku("DEL-001");
        p.setName("To Delete");
        repo.insert(p);

        Product found = repo.listAll().stream().filter(x -> "DEL-001".equals(x.getSku())).findFirst().orElse(null);
        assertNotNull(found);

        boolean deleted = repo.delete(found.getProductId());
        assertTrue(deleted);
        assertNull(repo.findById(found.getProductId()));
    }
}
