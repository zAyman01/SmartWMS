package com.warehousewms.repository;

import com.warehousewms.model.PurchaseOrder;
import com.warehousewms.model.PurchaseOrderLine;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderRepository {
    private final DataSource dataSource;

    public PurchaseOrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // -- Purchase Orders --

    public List<PurchaseOrder> listAll() throws SQLException {
        String sql = "SELECT POId, SupplierId, OrderDate, Status, Notes FROM PurchaseOrders ORDER BY OrderDate DESC";
        List<PurchaseOrder> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapPO(rs));
            }
        }
        return list;
    }

    public PurchaseOrder findById(int poId) throws SQLException {
        String sql = "SELECT POId, SupplierId, OrderDate, Status, Notes FROM PurchaseOrders WHERE POId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, poId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapPO(rs);
            }
        }
        return null;
    }

    public void insert(PurchaseOrder po) throws SQLException {
        String sql = "INSERT INTO PurchaseOrders (SupplierId, OrderDate, Status, Notes) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, po.getSupplierId());
            ps.setTimestamp(2, new Timestamp(po.getOrderDate().getTime()));
            ps.setString(3, po.getStatus());
            ps.setString(4, po.getNotes());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) po.setPoId(rs.getInt(1));
        }
    }

    public void update(PurchaseOrder po) throws SQLException {
        String sql = "UPDATE PurchaseOrders SET SupplierId=?, OrderDate=?, Status=?, Notes=? WHERE POId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, po.getSupplierId());
            ps.setTimestamp(2, new Timestamp(po.getOrderDate().getTime()));
            ps.setString(3, po.getStatus());
            ps.setString(4, po.getNotes());
            ps.setInt(5, po.getPoId());
            ps.executeUpdate();
        }
    }

    // -- Purchase Order Lines --

    public List<PurchaseOrderLine> findLinesByPoId(int poId) throws SQLException {
        String sql = "SELECT POLineId, POId, ProductId, QuantityOrdered, QuantityReceived FROM PurchaseOrderLines WHERE POId = ?";
        List<PurchaseOrderLine> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, poId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapPOLine(rs));
            }
        }
        return list;
    }

    public void insertLine(PurchaseOrderLine line) throws SQLException {
        String sql = "INSERT INTO PurchaseOrderLines (POId, ProductId, QuantityOrdered, QuantityReceived) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, line.getPoId());
            ps.setInt(2, line.getProductId());
            ps.setInt(3, line.getQuantityOrdered());
            ps.setInt(4, line.getQuantityReceived());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) line.setPoLineId(rs.getInt(1));
        }
    }

    public void updateLine(PurchaseOrderLine line) throws SQLException {
        String sql = "UPDATE PurchaseOrderLines SET POId=?, ProductId=?, QuantityOrdered=?, QuantityReceived=? WHERE POLineId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, line.getPoId());
            ps.setInt(2, line.getProductId());
            ps.setInt(3, line.getQuantityOrdered());
            ps.setInt(4, line.getQuantityReceived());
            ps.setInt(5, line.getPoLineId());
            ps.executeUpdate();
        }
    }

    public void deleteLine(int poLineId) throws SQLException {
        String sql = "DELETE FROM PurchaseOrderLines WHERE POLineId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, poLineId);
            ps.executeUpdate();
        }
    }

    private PurchaseOrder mapPO(ResultSet rs) throws SQLException {
        PurchaseOrder po = new PurchaseOrder();
        po.setPoId(rs.getInt("POId"));
        po.setSupplierId(rs.getInt("SupplierId"));
        po.setOrderDate(rs.getTimestamp("OrderDate"));
        po.setStatus(rs.getString("Status"));
        po.setNotes(rs.getString("Notes"));
        return po;
    }

    private PurchaseOrderLine mapPOLine(ResultSet rs) throws SQLException {
        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setPoLineId(rs.getInt("POLineId"));
        line.setPoId(rs.getInt("POId"));
        line.setProductId(rs.getInt("ProductId"));
        line.setQuantityOrdered(rs.getInt("QuantityOrdered"));
        line.setQuantityReceived(rs.getInt("QuantityReceived"));
        return line;
    }
}
