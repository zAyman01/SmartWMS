package com.warehousewms.service;

import com.warehousewms.model.AuditLog;
import com.warehousewms.model.Inventory;
import com.warehousewms.repository.AuditLogRepository;
import com.warehousewms.repository.InventoryRepository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class InventoryService {
    private final InventoryRepository invRepo;
    private final AuditLogRepository auditRepo;

    public InventoryService(DataSource dataSource) {
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
            if (newVal < 0) newVal = 0; // Prevent negative stock
            inv.setQuantity(newVal);
            invRepo.update(inv);
        } else {
            if (quantityDelta < 0) return; // Cannot adjust below zero if it doesn't exist
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

        Inventory fromInv = invRepo.findByProductAndBin(productId, fromBinId);
        if (fromInv == null || fromInv.getQuantity() < quantity) {
            throw new SQLException("Insufficient stock to transfer");
        }

        // Deduct from source
        int oldFrom = fromInv.getQuantity();
        fromInv.setQuantity(oldFrom - quantity);
        invRepo.update(fromInv);
        logAudit("Inventory", fromInv.getInventoryId(), "UPDATE", "Quantity", String.valueOf(oldFrom), String.valueOf(fromInv.getQuantity()), userId);

        // Add to destination
        Inventory toInv = invRepo.findByProductAndBin(productId, toBinId);
        int oldTo = 0;
        if (toInv != null) {
            oldTo = toInv.getQuantity();
            toInv.setQuantity(oldTo + quantity);
            invRepo.update(toInv);
        } else {
            toInv = new Inventory();
            toInv.setProductId(productId);
            toInv.setBinId(toBinId);
            toInv.setQuantity(quantity);
            invRepo.insert(toInv);
        }
        logAudit("Inventory", toInv.getInventoryId(), "UPDATE", "Quantity", String.valueOf(oldTo), String.valueOf(toInv.getQuantity()), userId);
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
}
