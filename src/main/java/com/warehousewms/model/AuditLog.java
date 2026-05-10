package com.warehousewms.model;

import java.util.Date;

public class AuditLog {
    private int auditId;
    private String tableName;
    private int recordId;
    private String actionType;
    private String columnName;
    private String oldValue;
    private String newValue;
    private int changedByUserId;
    private Date changedAt;

    public int getAuditId() { return auditId; }
    public void setAuditId(int auditId) { this.auditId = auditId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public int getChangedByUserId() { return changedByUserId; }
    public void setChangedByUserId(int changedByUserId) { this.changedByUserId = changedByUserId; }

    public Date getChangedAt() { return changedAt; }
    public void setChangedAt(Date changedAt) { this.changedAt = changedAt; }
}
