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

        List<Bin> roots = repo.findRootBins();
        assertFalse(roots.isEmpty());
        assertTrue(roots.stream().anyMatch(b -> "Zone A".equals(b.getName())));
    }

    @Test
    void insertChildBinAndFindChildren() throws SQLException {
        // Create parent
        Bin parent = new Bin();
        parent.setName("Zone B");
        parent.setBinType("Zone");
        parent.setParentBinId(null);
        parent.setSortOrder(1);
        repo.insert(parent);

        Bin inserted = repo.findRootBins().stream()
                .filter(b -> "Zone B".equals(b.getName()))
                .findFirst().orElse(null);
        assertNotNull(inserted);

        // Create child
        Bin child = new Bin();
        child.setName("Aisle 1");
        child.setBinType("Aisle");
        child.setParentBinId(inserted.getBinId());
        child.setSortOrder(1);
        repo.insert(child);

        List<Bin> children = repo.findChildren(inserted.getBinId());
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

        Bin found = repo.listAll().stream()
                .filter(b -> "Test Bin".equals(b.getName()))
                .findFirst().orElse(null);
        assertNotNull(found);

        Bin byId = repo.findById(found.getBinId());
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

        Bin found = repo.listAll().stream()
                .filter(b -> "Old Bin".equals(b.getName()))
                .findFirst().orElse(null);
        assertNotNull(found);

        found.setName("New Bin");
        found.setMaxWeightKg(200.0);
        repo.update(found);

        Bin updated = repo.findById(found.getBinId());
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

        Bin found = repo.listAll().stream()
                .filter(b -> "Deletable".equals(b.getName()))
                .findFirst().orElse(null);
        assertNotNull(found);

        assertTrue(repo.delete(found.getBinId()));
        assertNull(repo.findById(found.getBinId()));
    }

    @Test
    void toStringFormat() {
        Bin bin = new Bin();
        bin.setName("Shelf-01");
        bin.setBinType("Shelf");
        assertEquals("Shelf-01 [Shelf]", bin.toString());
    }
}
