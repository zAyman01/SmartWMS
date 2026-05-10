package com.warehousewms.repository;

import com.warehousewms.model.Bin;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BinRepository {
    private final DataSource dataSource;

    public BinRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Bin findById(int binId) throws SQLException {
        String sql = "SELECT BinId, ParentBinId, Name, BinType, MaxWeightKg, MaxVolumeM3, SortOrder FROM Bins WHERE BinId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, binId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapBin(rs);
            }
        }
        return null;
    }

    public List<Bin> findRootBins() throws SQLException {
        String sql = "SELECT BinId, ParentBinId, Name, BinType, MaxWeightKg, MaxVolumeM3, SortOrder FROM Bins WHERE ParentBinId IS NULL ORDER BY SortOrder, Name";
        List<Bin> bins = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bins.add(mapBin(rs));
            }
        }
        return bins;
    }

    public List<Bin> findChildren(int parentBinId) throws SQLException {
        String sql = "SELECT BinId, ParentBinId, Name, BinType, MaxWeightKg, MaxVolumeM3, SortOrder FROM Bins WHERE ParentBinId = ? ORDER BY SortOrder, Name";
        List<Bin> bins = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, parentBinId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                bins.add(mapBin(rs));
            }
        }
        return bins;
    }

    public List<Bin> listAll() throws SQLException {
        String sql = "SELECT BinId, ParentBinId, Name, BinType, MaxWeightKg, MaxVolumeM3, SortOrder FROM Bins ORDER BY SortOrder, Name";
        List<Bin> bins = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bins.add(mapBin(rs));
            }
        }
        return bins;
    }

    public void insert(Bin bin) throws SQLException {
        String sql = "INSERT INTO Bins (ParentBinId, Name, BinType, MaxWeightKg, MaxVolumeM3, SortOrder) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (bin.getParentBinId() != null) {
                ps.setInt(1, bin.getParentBinId());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, bin.getName());
            ps.setString(3, bin.getBinType());
            ps.setDouble(4, bin.getMaxWeightKg());
            ps.setDouble(5, bin.getMaxVolumeM3());
            ps.setInt(6, bin.getSortOrder());
            ps.executeUpdate();
        }
    }

    public void update(Bin bin) throws SQLException {
        String sql = "UPDATE Bins SET ParentBinId = ?, Name = ?, BinType = ?, MaxWeightKg = ?, MaxVolumeM3 = ?, SortOrder = ? WHERE BinId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (bin.getParentBinId() != null) {
                ps.setInt(1, bin.getParentBinId());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, bin.getName());
            ps.setString(3, bin.getBinType());
            ps.setDouble(4, bin.getMaxWeightKg());
            ps.setDouble(5, bin.getMaxVolumeM3());
            ps.setInt(6, bin.getSortOrder());
            ps.setInt(7, bin.getBinId());
            ps.executeUpdate();
        }
    }

    public boolean delete(int binId) throws SQLException {
        String sql = "DELETE FROM Bins WHERE BinId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, binId);
            return ps.executeUpdate() > 0;
        }
    }

    private Bin mapBin(ResultSet rs) throws SQLException {
        Bin b = new Bin();
        b.setBinId(rs.getInt("BinId"));
        int parentId = rs.getInt("ParentBinId");
        b.setParentBinId(rs.wasNull() ? null : parentId);
        b.setName(rs.getString("Name"));
        b.setBinType(rs.getString("BinType"));
        b.setMaxWeightKg(rs.getDouble("MaxWeightKg"));
        b.setMaxVolumeM3(rs.getDouble("MaxVolumeM3"));
        b.setSortOrder(rs.getInt("SortOrder"));
        return b;
    }
}
