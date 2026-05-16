package com.warehousewms.repository;

import com.warehousewms.model.Supplier;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplierRepository {
    private final DataSource dataSource;

    public SupplierRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Supplier findById(int supplierId) throws SQLException {
        String sql = "SELECT SupplierId, Name, ContactName, Email, Phone FROM Suppliers WHERE SupplierId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapSupplier(rs);
            }
        }
        return null;
    }

    public List<Supplier> listAll() throws SQLException {
        String sql = "SELECT SupplierId, Name, ContactName, Email, Phone FROM Suppliers ORDER BY Name";
        List<Supplier> suppliers = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                suppliers.add(mapSupplier(rs));
            }
        }
        return suppliers;
    }

    public void insert(Supplier supplier) throws SQLException {
        String sql = "INSERT INTO Suppliers (Name, ContactName, Email, Phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getContactName());
            ps.setString(3, supplier.getEmail());
            ps.setString(4, supplier.getPhone());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                supplier.setSupplierId(rs.getInt(1));
            }
        }
    }

    public void update(Supplier supplier) throws SQLException {
        String sql = "UPDATE Suppliers SET Name = ?, ContactName = ?, Email = ?, Phone = ? WHERE SupplierId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getContactName());
            ps.setString(3, supplier.getEmail());
            ps.setString(4, supplier.getPhone());
            ps.setInt(5, supplier.getSupplierId());
            ps.executeUpdate();
        }
    }

    public boolean delete(int supplierId) throws SQLException {
        String sql = "DELETE FROM Suppliers WHERE SupplierId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            return ps.executeUpdate() > 0;
        }
    }

    private Supplier mapSupplier(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setSupplierId(rs.getInt("SupplierId"));
        s.setName(rs.getString("Name"));
        s.setContactName(rs.getString("ContactName"));
        s.setEmail(rs.getString("Email"));
        s.setPhone(rs.getString("Phone"));
        return s;
    }
}
