package com.warehousewms.service;

import com.warehousewms.config.ConnectionPool;
import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.Bin;
import com.warehousewms.model.Inventory;
import com.warehousewms.model.Product;
import com.warehousewms.repository.BinRepository;
import com.warehousewms.repository.InventoryRepository;
import com.warehousewms.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest {
    private DataSource ds;
    private InventoryService service;
    private InventoryRepository invRepo;
    private int productId;
    private int binId1;
    private int binId2;

    @BeforeEach
    void setUp() throws SQLException {
        System.setProperty("wms.useSqlServer", "false");
        ds = new DatabaseManager().getDataSourceWithFallback();
        service = new InventoryService(ds);
        invRepo = new InventoryRepository(ds);

        ProductRepository prodRepo = new ProductRepository(ds);
        BinRepository binRepo = new BinRepository(ds);

        // Create test product
        Product p = new Product();
        p.setSku("SVC-P-" + System.nanoTime());
        p.setName("Service Test Product");
        prodRepo.insert(p);
        productId = prodRepo.listAll().stream()
                .filter(x -> x.getSku().equals(p.getSku()))
                .findFirst().orElseThrow().getProductId();

        // Create two test bins
        Bin b1 = new Bin();
        b1.setName("SvcBin1-" + System.nanoTime());
        b1.setBinType("Location");
        b1.setSortOrder(0);
        binRepo.insert(b1);
        binId1 = binRepo.listAll().stream()
                .filter(x -> x.getName().equals(b1.getName()))
                .findFirst().orElseThrow().getBinId();

        Bin b2 = new Bin();
        b2.setName("SvcBin2-" + System.nanoTime());
        b2.setBinType("Location");
        b2.setSortOrder(0);
        binRepo.insert(b2);
        binId2 = binRepo.listAll().stream()
                .filter(x -> x.getName().equals(b2.getName()))
                .findFirst().orElseThrow().getBinId();
    }

    @AfterEach
    void tearDown() {
        ConnectionPool.shutdown();
        System.clearProperty("wms.useSqlServer");
    }

    @Test
    void adjustStockCreatesNewRecord() throws SQLException {
        // userId=1 is the admin
        service.adjustStock(productId, binId1, 50, 1, "Test add");

        Inventory inv = invRepo.findByProductAndBin(productId, binId1);
        assertNotNull(inv);
        assertEquals(50, inv.getQuantity());
    }

    @Test
    void adjustStockUpdatesExistingRecord() throws SQLException {
        service.adjustStock(productId, binId1, 30, 1, "Initial");
        service.adjustStock(productId, binId1, 20, 1, "Add more");

        Inventory inv = invRepo.findByProductAndBin(productId, binId1);
        assertNotNull(inv);
        assertEquals(50, inv.getQuantity());
    }

    @Test
    void adjustStockNegativePreventsNegativeQuantity() throws SQLException {
        service.adjustStock(productId, binId1, 10, 1, "Initial");
        service.adjustStock(productId, binId1, -100, 1, "Large deduction");

        Inventory inv = invRepo.findByProductAndBin(productId, binId1);
        assertNotNull(inv);
        assertEquals(0, inv.getQuantity(), "Quantity should not go below zero");
    }

    @Test
    void adjustStockZeroDeltaIsNoop() throws SQLException {
        service.adjustStock(productId, binId1, 10, 1, "Initial");
        service.adjustStock(productId, binId1, 0, 1, "No change");

        Inventory inv = invRepo.findByProductAndBin(productId, binId1);
        assertEquals(10, inv.getQuantity());
    }

    @Test
    void transferStockMovesBetweenBins() throws SQLException {
        // Seed source bin
        service.adjustStock(productId, binId1, 100, 1, "Seed");

        // Transfer 40 from bin1 to bin2
        service.transferStock(productId, binId1, binId2, 40, 1);

        Inventory from = invRepo.findByProductAndBin(productId, binId1);
        Inventory to = invRepo.findByProductAndBin(productId, binId2);

        assertNotNull(from);
        assertNotNull(to);
        assertEquals(60, from.getQuantity());
        assertEquals(40, to.getQuantity());
    }

    @Test
    void transferStockFailsOnInsufficientStock() {
        assertThrows(SQLException.class, () ->
                service.transferStock(productId, binId1, binId2, 10, 1),
                "Should throw when source has no stock");
    }

    @Test
    void transferStockFailsWhenTransferExceedsAvailable() throws SQLException {
        service.adjustStock(productId, binId1, 5, 1, "Small stock");

        assertThrows(SQLException.class, () ->
                service.transferStock(productId, binId1, binId2, 10, 1),
                "Should throw when transfer quantity exceeds available");
    }
}
