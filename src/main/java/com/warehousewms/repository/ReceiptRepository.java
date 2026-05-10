package com.warehousewms.repository;

import com.warehousewms.model.Receipt;
import com.warehousewms.model.ReceiptLine;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReceiptRepository {
    private final DataSource dataSource;

    public ReceiptRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Receipt> listAll() throws SQLException {
        String sql = "SELECT ReceiptId, POId, ReceiptDate, Status, Notes FROM Receipts ORDER BY ReceiptDate DESC";
        List<Receipt> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapReceipt(rs));
            }
        }
        return list;
    }

    public Receipt findById(int receiptId) throws SQLException {
        String sql = "SELECT ReceiptId, POId, ReceiptDate, Status, Notes FROM Receipts WHERE ReceiptId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, receiptId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapReceipt(rs);
            }
        }
        return null;
    }

    public void insert(Receipt receipt) throws SQLException {
        String sql = "INSERT INTO Receipts (POId, ReceiptDate, Status, Notes) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (receipt.getPoId() != null) ps.setInt(1, receipt.getPoId());
            else ps.setNull(1, Types.INTEGER);
            ps.setTimestamp(2, new Timestamp(receipt.getReceiptDate().getTime()));
            ps.setString(3, receipt.getStatus());
            ps.setString(4, receipt.getNotes());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) receipt.setReceiptId(rs.getInt(1));
        }
    }

    public List<ReceiptLine> findLinesByReceiptId(int receiptId) throws SQLException {
        String sql = "SELECT ReceiptLineId, ReceiptId, ProductId, BinId, Quantity, LotNumber, ExpiryDate FROM ReceiptLines WHERE ReceiptId = ?";
        List<ReceiptLine> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, receiptId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapReceiptLine(rs));
            }
        }
        return list;
    }

    public void insertLine(ReceiptLine line) throws SQLException {
        String sql = "INSERT INTO ReceiptLines (ReceiptId, ProductId, BinId, Quantity, LotNumber, ExpiryDate) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, line.getReceiptId());
            ps.setInt(2, line.getProductId());
            ps.setInt(3, line.getBinId());
            ps.setInt(4, line.getQuantity());
            ps.setString(5, line.getLotNumber());
            if (line.getExpiryDate() != null) ps.setDate(6, new java.sql.Date(line.getExpiryDate().getTime()));
            else ps.setNull(6, Types.DATE);
            
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) line.setReceiptLineId(rs.getInt(1));
        }
    }

    private Receipt mapReceipt(ResultSet rs) throws SQLException {
        Receipt r = new Receipt();
        r.setReceiptId(rs.getInt("ReceiptId"));
        int poId = rs.getInt("POId");
        if (!rs.wasNull()) r.setPoId(poId);
        r.setReceiptDate(rs.getTimestamp("ReceiptDate"));
        r.setStatus(rs.getString("Status"));
        r.setNotes(rs.getString("Notes"));
        return r;
    }

    private ReceiptLine mapReceiptLine(ResultSet rs) throws SQLException {
        ReceiptLine line = new ReceiptLine();
        line.setReceiptLineId(rs.getInt("ReceiptLineId"));
        line.setReceiptId(rs.getInt("ReceiptId"));
        line.setProductId(rs.getInt("ProductId"));
        line.setBinId(rs.getInt("BinId"));
        line.setQuantity(rs.getInt("Quantity"));
        line.setLotNumber(rs.getString("LotNumber"));
        Date exp = rs.getDate("ExpiryDate");
        if (exp != null) line.setExpiryDate(new java.util.Date(exp.getTime()));
        return line;
    }
}
