package com.warehousewms.repository;

import com.warehousewms.config.ConnectionPool;
import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.Bin;
import com.warehousewms.model.Inventory;
import com.warehousewms.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryRepositoryTest {
    private DataSource ds;
    private InventoryRepository invRepo;
    private ProductRepository prodRepo;
    private BinRepository binRepo;
    private int productId;
    private int binId;

    @BeforeEach
    void setUp() throws SQLException {
        System.setProperty("wms.useSqlServer", "false");
        ds = new DatabaseManager().getDataSourceWithFallback();
        invRepo = new InventoryRepository(ds);
        prodRepo = new ProductRepository(ds);
        binRepo = new BinRepository(ds);

        // Create a product and bin for inventory tests
        Product p = new Product();
        p.setSku("INV-P-" + System.nanoTime());
        p.setName("Inventory Test Product");
        prodRepo.insert(p);
        productId = prodRepo.listAll().stream()
                .filter(x -> x.getSku().equals(p.getSku()))
                .findFirst().orElseThrow().getProductId();

        Bin b = new Bin();
        b.setName("InvBin-" + System.nanoTime());
        b.setBinType("Location");
        b.setSortOrder(0);
        binRepo.insert(b);
        binId = binRepo.listAll().stream()
                .filter(x -> x.getName().equals(b.getName()))
                .findFirst().orElseThrow().getBinId();
    }

    @AfterEach
    void tearDown() {
        ConnectionPool.shutdown();
        System.clearProperty("wms.useSqlServer");
    }

    @Test
    void insertAndFindById() throws SQLException {
        Inventory inv = new Inventory();
        inv.setProductId(productId);
        inv.setBinId(binId);
        inv.setQuantity(50);
        invRepo.insert(inv);

        assertTrue(inv.getInventoryId() > 0, "Insert should set generated ID");

        Inventory found = invRepo.findById(inv.getInventoryId());
        assertNotNull(found);
        assertEquals(50, found.getQuantity());
        assertEquals(productId, found.getProductId());
        assertEquals(binId, found.getBinId());
    }

    @Test
    void findByProductAndBin() throws SQLException {
        Inventory inv = new Inventory();
        inv.setProductId(productId);
        inv.setBinId(binId);
        inv.setQuantity(25);
        invRepo.insert(inv);

        Inventory found = invRepo.findByProductAndBin(productId, binId);
        assertNotNull(found);
        assertEquals(25, found.getQuantity());
    }

    @Test
    void findByProductId() throws SQLException {
        Inventory inv = new Inventory();
        inv.setProductId(productId);
        inv.setBinId(binId);
        inv.setQuantity(10);
        invRepo.insert(inv);

        List<Inventory> list = invRepo.findByProductId(productId);
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(i -> i.getQuantity() == 10));
    }

    @Test
    void updateInventory() throws SQLException {
        Inventory inv = new Inventory();
        inv.setProductId(productId);
        inv.setBinId(binId);
        inv.setQuantity(30);
        invRepo.insert(inv);

        inv.setQuantity(100);
        invRepo.update(inv);

        Inventory updated = invRepo.findById(inv.getInventoryId());
        assertEquals(100, updated.getQuantity());
    }

    @Test
    void listAll() throws SQLException {
        Inventory inv = new Inventory();
        inv.setProductId(productId);
        inv.setBinId(binId);
        inv.setQuantity(5);
        invRepo.insert(inv);

        List<Inventory> all = invRepo.listAll();
        assertFalse(all.isEmpty());
    }
}
