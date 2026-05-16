package com.warehousewms.service;

import com.warehousewms.model.AuditLog;
import com.warehousewms.model.Inventory;
import com.warehousewms.repository.AuditLogRepository;
import com.warehousewms.repository.InventoryRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class InventoryService {
    private final DataSource dataSource;
    private final InventoryRepository invRepo;
    private final AuditLogRepository auditRepo;

    public InventoryService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.invRepo = new InventoryRepository(dataSource);
        this.auditRepo = new AuditLogRepository(dataSource);
    }

    public List<Inventory> getAllInventory() throws SQLException {
        return invRepo.listAll();
    }

    public void adjustStock(int productId, int binId, int quantityDelta, int userId, String reason) throws SQLException {
        if (quantityDelta == 0) return;

        Inventory inv = invRepo.findByProductAndBin(productId, binId);
        int oldVal = 0;
        int newVal = quantityDelta;

        if (inv != null) {
            oldVal = inv.getQuantity();
            newVal = oldVal + quantityDelta;
            if (newVal < 0) newVal = 0;
            inv.setQuantity(newVal);
            invRepo.update(inv);
        } else {
            if (quantityDelta < 0) return;
            inv = new Inventory();
            inv.setProductId(productId);
            inv.setBinId(binId);
            inv.setQuantity(newVal);
            invRepo.insert(inv);
        }

        logAudit("Inventory", inv.getInventoryId(), "UPDATE", "Quantity", String.valueOf(oldVal), String.valueOf(newVal), userId);
    }

    public void transferStock(int productId, int fromBinId, int toBinId, int quantity, int userId) throws SQLException {
        if (quantity <= 0) return;

        Connection conn = dataSource.getConnection();
        try {
            conn.setAutoCommit(false);

            Inventory fromInv = invRepo.findByProductAndBin(productId, fromBinId, conn);
            if (fromInv == null || fromInv.getQuantity() < quantity) {
                throw new SQLException("Insufficient stock to transfer");
            }

            int oldFrom = fromInv.getQuantity();
            fromInv.setQuantity(oldFrom - quantity);
            invRepo.update(fromInv, conn);
            logAudit("Inventory", fromInv.getInventoryId(), "UPDATE", "Quantity",
                    String.valueOf(oldFrom), String.valueOf(fromInv.getQuantity()), userId, conn);

            Inventory toInv = invRepo.findByProductAndBin(productId, toBinId, conn);
            int oldTo = 0;
            if (toInv != null) {
                oldTo = toInv.getQuantity();
                toInv.setQuantity(oldTo + quantity);
                invRepo.update(toInv, conn);
            } else {
                toInv = new Inventory();
                toInv.setProductId(productId);
                toInv.setBinId(toBinId);
                toInv.setQuantity(quantity);
                invRepo.insert(toInv, conn);
            }
            logAudit("Inventory", toInv.getInventoryId(), "UPDATE", "Quantity",
                    String.valueOf(oldTo), String.valueOf(toInv.getQuantity()), userId, conn);

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    private void logAudit(String table, int recordId, String action, String col, String oldV, String newV, int userId) throws SQLException {
        AuditLog log = new AuditLog();
        log.setTableName(table);
        log.setRecordId(recordId);
        log.setActionType(action);
        log.setColumnName(col);
        log.setOldValue(oldV);
        log.setNewValue(newV);
        log.setChangedByUserId(userId);
        auditRepo.insert(log);
    }

    private void logAudit(String table, int recordId, String action, String col, String oldV, String newV, int userId, Connection conn) throws SQLException {
        AuditLog log = new AuditLog();
        log.setTableName(table);
        log.setRecordId(recordId);
        log.setActionType(action);
        log.setColumnName(col);
        log.setOldValue(oldV);
        log.setNewValue(newV);
        log.setChangedByUserId(userId);
        auditRepo.insert(log, conn);
    }
}
