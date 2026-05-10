package com.warehousewms.repository;

import com.warehousewms.model.AuditLog;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogRepository {
    private final DataSource dataSource;

    public AuditLogRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(AuditLog log) throws SQLException {
        String sql = "INSERT INTO AuditLog (TableName, RecordId, ActionType, ColumnName, OldValue, NewValue, ChangedByUserId, ChangedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, log.getTableName());
            ps.setInt(2, log.getRecordId());
            ps.setString(3, log.getActionType());
            ps.setString(4, log.getColumnName());
            ps.setString(5, log.getOldValue());
            ps.setString(6, log.getNewValue());
            ps.setInt(7, log.getChangedByUserId());
            ps.setTimestamp(8, new Timestamp(log.getChangedAt() != null ? log.getChangedAt().getTime() : System.currentTimeMillis()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) log.setAuditId(rs.getInt(1));
        }
    }

    public List<AuditLog> findByTableAndRecord(String tableName, int recordId) throws SQLException {
        String sql = "SELECT AuditId, TableName, RecordId, ActionType, ColumnName, OldValue, NewValue, ChangedByUserId, ChangedAt FROM AuditLog WHERE TableName = ? AND RecordId = ? ORDER BY ChangedAt DESC";
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setInt(2, recordId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapAuditLog(rs));
            }
        }
        return list;
    }

    private AuditLog mapAuditLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setAuditId(rs.getInt("AuditId"));
        log.setTableName(rs.getString("TableName"));
        log.setRecordId(rs.getInt("RecordId"));
        log.setActionType(rs.getString("ActionType"));
        log.setColumnName(rs.getString("ColumnName"));
        log.setOldValue(rs.getString("OldValue"));
        log.setNewValue(rs.getString("NewValue"));
        log.setChangedByUserId(rs.getInt("ChangedByUserId"));
        log.setChangedAt(rs.getTimestamp("ChangedAt"));
        return log;
    }
}
