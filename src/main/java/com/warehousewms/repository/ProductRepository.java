package com.warehousewms.repository;

import com.warehousewms.model.Product;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
    private final DataSource dataSource;

    public ProductRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Product findById(int productId) throws SQLException {
        String sql = "SELECT ProductId, SKU, Name, ImagePath, UnitWeightKg, UnitVolumeM3, IsActive FROM Products WHERE ProductId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapProduct(rs);
            }
        }
        return null;
    }

    public boolean skuExists(String sku) throws SQLException {
        String sql = "SELECT 1 FROM Products WHERE SKU = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Product> listAll() throws SQLException {
        String sql = "SELECT ProductId, SKU, Name, ImagePath, UnitWeightKg, UnitVolumeM3, IsActive FROM Products ORDER BY Name";
        List<Product> products = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        }
        return products;
    }

    public List<Product> listActive() throws SQLException {
        String sql = "SELECT ProductId, SKU, Name, ImagePath, UnitWeightKg, UnitVolumeM3, IsActive FROM Products WHERE IsActive = TRUE ORDER BY Name";
        List<Product> products = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        }
        return products;
    }

    public void insert(Product product) throws SQLException {
        String sql = "INSERT INTO Products (SKU, Name, ImagePath, UnitWeightKg, UnitVolumeM3, IsActive) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getSku());
            ps.setString(2, product.getName());
            ps.setString(3, product.getImagePath());
            ps.setDouble(4, product.getUnitWeightKg());
            ps.setDouble(5, product.getUnitVolumeM3());
            ps.setBoolean(6, product.isActive());
            ps.executeUpdate();
        }
    }

    public void update(Product product) throws SQLException {
        String sql = "UPDATE Products SET SKU = ?, Name = ?, ImagePath = ?, UnitWeightKg = ?, UnitVolumeM3 = ?, IsActive = ? WHERE ProductId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getSku());
            ps.setString(2, product.getName());
            ps.setString(3, product.getImagePath());
            ps.setDouble(4, product.getUnitWeightKg());
            ps.setDouble(5, product.getUnitVolumeM3());
            ps.setBoolean(6, product.isActive());
            ps.setInt(7, product.getProductId());
            ps.executeUpdate();
        }
    }

    public boolean delete(int productId) throws SQLException {
        String sql = "DELETE FROM Products WHERE ProductId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("ProductId"));
        p.setSku(rs.getString("SKU"));
        p.setName(rs.getString("Name"));
        p.setImagePath(rs.getString("ImagePath"));
        p.setUnitWeightKg(rs.getDouble("UnitWeightKg"));
        p.setUnitVolumeM3(rs.getDouble("UnitVolumeM3"));
        p.setActive(rs.getBoolean("IsActive"));
        return p;
    }
}
