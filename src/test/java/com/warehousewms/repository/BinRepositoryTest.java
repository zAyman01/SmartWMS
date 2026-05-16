package com.warehousewms.repository;

import com.warehousewms.config.ConnectionPool;
import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.Bin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BinRepositoryTest {
    private DataSource ds;
    private BinRepository repo;

    @BeforeEach
    void setUp() {
        System.setProperty("wms.useSqlServer", "false");
        ds = new DatabaseManager().getDataSourceWithFallback();
        repo = new BinRepository(ds);
    }

    @AfterEach
    void tearDown() {
        ConnectionPool.shutdown();
        System.clearProperty("wms.useSqlServer");
    }

    @Test
    void insertRootBinAndFindRoots() throws SQLException {
        Bin zone = new Bin();
        zone.setName("Zone A");
        zone.setBinType("Zone");
        zone.setParentBinId(null);
        zone.setSortOrder(1);
        repo.insert(zone);
        assertTrue(zone.getBinId() > 0, "insert should set generated binId");

        List<Bin> roots = repo.findRootBins();
        assertFalse(roots.isEmpty());
        assertTrue(roots.stream().anyMatch(b -> "Zone A".equals(b.getName())));
    }

    @Test
    void insertChildBinAndFindChildren() throws SQLException {
        Bin parent = new Bin();
        parent.setName("Zone B");
        parent.setBinType("Zone");
        parent.setParentBinId(null);
        parent.setSortOrder(1);
        repo.insert(parent);
        assertTrue(parent.getBinId() > 0);

        Bin child = new Bin();
        child.setName("Aisle 1");
        child.setBinType("Aisle");
        child.setParentBinId(parent.getBinId());
        child.setSortOrder(1);
        repo.insert(child);
        assertTrue(child.getBinId() > 0);

        List<Bin> children = repo.findChildren(parent.getBinId());
        assertEquals(1, children.size());
        assertEquals("Aisle 1", children.get(0).getName());
    }

    @Test
    void findById() throws SQLException {
        Bin bin = new Bin();
        bin.setName("Test Bin");
        bin.setBinType("Location");
        bin.setSortOrder(0);
        repo.insert(bin);
        assertTrue(bin.getBinId() > 0);

        Bin byId = repo.findById(bin.getBinId());
        assertNotNull(byId);
        assertEquals("Test Bin", byId.getName());
        assertEquals("Location", byId.getBinType());
    }

    @Test
    void updateBin() throws SQLException {
        Bin bin = new Bin();
        bin.setName("Old Bin");
        bin.setBinType("Shelf");
        bin.setMaxWeightKg(100.0);
        bin.setSortOrder(0);
        repo.insert(bin);
        assertTrue(bin.getBinId() > 0);

        bin.setName("New Bin");
        bin.setMaxWeightKg(200.0);
        repo.update(bin);

        Bin updated = repo.findById(bin.getBinId());
        assertEquals("New Bin", updated.getName());
        assertEquals(200.0, updated.getMaxWeightKg(), 0.001);
    }

    @Test
    void deleteBin() throws SQLException {
        Bin bin = new Bin();
        bin.setName("Deletable");
        bin.setBinType("Location");
        bin.setSortOrder(0);
        repo.insert(bin);
        assertTrue(bin.getBinId() > 0);

        assertTrue(repo.delete(bin.getBinId()));
        assertNull(repo.findById(bin.getBinId()));
    }

    @Test
    void toStringFormat() {
        Bin bin = new Bin();
        bin.setName("Shelf-01");
        bin.setBinType("Shelf");
        assertEquals("Shelf-01 [Shelf]", bin.toString());
    }
}
