package com.warehousewms.service;

import com.warehousewms.config.ConnectionPool;
import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.*;
import com.warehousewms.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseOrderServiceTest {
    private DataSource ds;
    private PurchaseOrderService poService;
    private int supplierId;
    private int productId;

    @BeforeEach
    void setUp() throws SQLException {
        System.setProperty("wms.useSqlServer", "false");
        ds = new DatabaseManager().getDataSourceWithFallback();
        poService = new PurchaseOrderService(ds);

        SupplierRepository supRepo = new SupplierRepository(ds);
        Supplier s = new Supplier();
        s.setName("POSup" + System.nanoTime());
        supRepo.insert(s);
        supplierId = supRepo.listAll().stream()
                .filter(x -> x.getName().equals(s.getName()))
                .findFirst().orElseThrow().getSupplierId();

        ProductRepository prodRepo = new ProductRepository(ds);
        Product p = new Product();
        p.setSku("PO-" + System.nanoTime());
        p.setName("PO Product");
        prodRepo.insert(p);
        productId = prodRepo.listAll().stream()
                .filter(x -> x.getSku().equals(p.getSku()))
                .findFirst().orElseThrow().getProductId();
    }

    @AfterEach
    void tearDown() {
        ConnectionPool.shutdown();
        System.clearProperty("wms.useSqlServer");
    }

    @Test
    void createAndGetPO() throws SQLException {
        PurchaseOrder po = new PurchaseOrder();
        po.setSupplierId(supplierId);
        po.setOrderDate(new Date());
        po.setStatus("Open");

        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setProductId(productId);
        line.setQuantityOrdered(100);
        List<PurchaseOrderLine> lines = new ArrayList<>();
        lines.add(line);

        poService.createPO(po, lines);
        assertTrue(po.getPoId() > 0);

        PurchaseOrder found = poService.getPOById(po.getPoId());
        assertNotNull(found);
        assertEquals("Open", found.getStatus());

        List<PurchaseOrderLine> fetched = poService.getLinesForPO(po.getPoId());
        assertEquals(1, fetched.size());
        assertEquals(100, fetched.get(0).getQuantityOrdered());
    }
}
