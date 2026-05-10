package com.warehousewms.repository;

import com.warehousewms.model.Inventory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryRepository {
    private final DataSource dataSource;

    public InventoryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Inventory findById(int inventoryId) throws SQLException {
        String sql = "SELECT InventoryId, ProductId, BinId, Quantity, LotNumber, ExpiryDate FROM Inventory WHERE InventoryId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inventoryId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapInventory(rs);
            }
        }
        return null;
    }

    public Inventory findByProductAndBin(int productId, int binId) throws SQLException {
        String sql = "SELECT InventoryId, ProductId, BinId, Quantity, LotNumber, ExpiryDate FROM Inventory WHERE ProductId = ? AND BinId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, binId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapInventory(rs);
            }
        }
        return null;
    }

    public List<Inventory> findByProductId(int productId) throws SQLException {
        String sql = "SELECT InventoryId, ProductId, BinId, Quantity, LotNumber, ExpiryDate FROM Inventory WHERE ProductId = ?";
        List<Inventory> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapInventory(rs));
            }
        }
        return list;
    }

    public List<Inventory> listAll() throws SQLException {
        String sql = "SELECT InventoryId, ProductId, BinId, Quantity, LotNumber, ExpiryDate FROM Inventory";
        List<Inventory> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapInventory(rs));
            }
        }
        return list;
    }

    public void insert(Inventory inv) throws SQLException {
        String sql = "INSERT INTO Inventory (ProductId, BinId, Quantity, LotNumber, ExpiryDate) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inv.getProductId());
            ps.setInt(2, inv.getBinId());
            ps.setInt(3, inv.getQuantity());
            ps.setString(4, inv.getLotNumber());
            if (inv.getExpiryDate() != null) {
                ps.setDate(5, new java.sql.Date(inv.getExpiryDate().getTime()));
            } else {
                ps.setNull(5, java.sql.Types.DATE);
            }
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                inv.setInventoryId(rs.getInt(1));
            }
        }
    }

    public void update(Inventory inv) throws SQLException {
        String sql = "UPDATE Inventory SET ProductId = ?, BinId = ?, Quantity = ?, LotNumber = ?, ExpiryDate = ? WHERE InventoryId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inv.getProductId());
            ps.setInt(2, inv.getBinId());
            ps.setInt(3, inv.getQuantity());
            ps.setString(4, inv.getLotNumber());
            if (inv.getExpiryDate() != null) {
                ps.setDate(5, new java.sql.Date(inv.getExpiryDate().getTime()));
            } else {
                ps.setNull(5, java.sql.Types.DATE);
            }
            ps.setInt(6, inv.getInventoryId());
            ps.executeUpdate();
        }
    }

    private Inventory mapInventory(ResultSet rs) throws SQLException {
        Inventory inv = new Inventory();
        inv.setInventoryId(rs.getInt("InventoryId"));
        inv.setProductId(rs.getInt("ProductId"));
        inv.setBinId(rs.getInt("BinId"));
        inv.setQuantity(rs.getInt("Quantity"));
        inv.setLotNumber(rs.getString("LotNumber"));
        java.sql.Date expiry = rs.getDate("ExpiryDate");
        if (expiry != null) {
            inv.setExpiryDate(new java.util.Date(expiry.getTime()));
        }
        return inv;
    }
}
