package com.warehousewms.repository;

import com.warehousewms.model.PickRun;
import com.warehousewms.model.PickRunItem;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PickRunRepository {
    private final DataSource dataSource;

    public PickRunRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<PickRun> listAll() throws SQLException {
        String sql = "SELECT PickRunId, AssignedToUserId, StartedAt, CompletedAt, Status FROM PickRuns ORDER BY PickRunId DESC";
        List<PickRun> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapPickRun(rs));
            }
        }
        return list;
    }

    public PickRun findById(int pickRunId) throws SQLException {
        String sql = "SELECT PickRunId, AssignedToUserId, StartedAt, CompletedAt, Status FROM PickRuns WHERE PickRunId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pickRunId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapPickRun(rs);
            }
        }
        return null;
    }

    public void insert(PickRun pr) throws SQLException {
        String sql = "INSERT INTO PickRuns (AssignedToUserId, StartedAt, CompletedAt, Status) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (pr.getAssignedToUserId() != null) ps.setInt(1, pr.getAssignedToUserId());
            else ps.setNull(1, Types.INTEGER);
            if (pr.getStartedAt() != null) ps.setTimestamp(2, new Timestamp(pr.getStartedAt().getTime()));
            else ps.setNull(2, Types.TIMESTAMP);
            if (pr.getCompletedAt() != null) ps.setTimestamp(3, new Timestamp(pr.getCompletedAt().getTime()));
            else ps.setNull(3, Types.TIMESTAMP);
            ps.setString(4, pr.getStatus());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) pr.setPickRunId(rs.getInt(1));
        }
    }

    public void update(PickRun pr) throws SQLException {
        String sql = "UPDATE PickRuns SET AssignedToUserId=?, StartedAt=?, CompletedAt=?, Status=? WHERE PickRunId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (pr.getAssignedToUserId() != null) ps.setInt(1, pr.getAssignedToUserId());
            else ps.setNull(1, Types.INTEGER);
            if (pr.getStartedAt() != null) ps.setTimestamp(2, new Timestamp(pr.getStartedAt().getTime()));
            else ps.setNull(2, Types.TIMESTAMP);
            if (pr.getCompletedAt() != null) ps.setTimestamp(3, new Timestamp(pr.getCompletedAt().getTime()));
            else ps.setNull(3, Types.TIMESTAMP);
            ps.setString(4, pr.getStatus());
            ps.setInt(5, pr.getPickRunId());
            ps.executeUpdate();
        }
    }

    public List<PickRunItem> findItemsByPickRunId(int pickRunId) throws SQLException {
        String sql = "SELECT PickRunItemId, PickRunId, OrderLineId, BinId, QuantityToPick, QuantityPicked, Status FROM PickRunItems WHERE PickRunId = ?";
        List<PickRunItem> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pickRunId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapPickRunItem(rs));
            }
        }
        return list;
    }

    public void insertItem(PickRunItem item) throws SQLException {
        String sql = "INSERT INTO PickRunItems (PickRunId, OrderLineId, BinId, QuantityToPick, QuantityPicked, Status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getPickRunId());
            ps.setInt(2, item.getOrderLineId());
            ps.setInt(3, item.getBinId());
            ps.setInt(4, item.getQuantityToPick());
            ps.setInt(5, item.getQuantityPicked());
            ps.setString(6, item.getStatus());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) item.setPickRunItemId(rs.getInt(1));
        }
    }

    public void updateItem(PickRunItem item) throws SQLException {
        String sql = "UPDATE PickRunItems SET PickRunId=?, OrderLineId=?, BinId=?, QuantityToPick=?, QuantityPicked=?, Status=? WHERE PickRunItemId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getPickRunId());
            ps.setInt(2, item.getOrderLineId());
            ps.setInt(3, item.getBinId());
            ps.setInt(4, item.getQuantityToPick());
            ps.setInt(5, item.getQuantityPicked());
            ps.setString(6, item.getStatus());
            ps.setInt(7, item.getPickRunItemId());
            ps.executeUpdate();
        }
    }

    private PickRun mapPickRun(ResultSet rs) throws SQLException {
        PickRun pr = new PickRun();
        pr.setPickRunId(rs.getInt("PickRunId"));
        int assigned = rs.getInt("AssignedToUserId");
        if (!rs.wasNull()) pr.setAssignedToUserId(assigned);
        Timestamp started = rs.getTimestamp("StartedAt");
        if (started != null) pr.setStartedAt(new java.util.Date(started.getTime()));
        Timestamp completed = rs.getTimestamp("CompletedAt");
        if (completed != null) pr.setCompletedAt(new java.util.Date(completed.getTime()));
        pr.setStatus(rs.getString("Status"));
        return pr;
    }

    private PickRunItem mapPickRunItem(ResultSet rs) throws SQLException {
        PickRunItem item = new PickRunItem();
        item.setPickRunItemId(rs.getInt("PickRunItemId"));
        item.setPickRunId(rs.getInt("PickRunId"));
        item.setOrderLineId(rs.getInt("OrderLineId"));
        item.setBinId(rs.getInt("BinId"));
        item.setQuantityToPick(rs.getInt("QuantityToPick"));
        item.setQuantityPicked(rs.getInt("QuantityPicked"));
        item.setStatus(rs.getString("Status"));
        return item;
    }
}
