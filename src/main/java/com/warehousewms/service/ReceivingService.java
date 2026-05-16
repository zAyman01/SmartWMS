package com.warehousewms.service;

import com.warehousewms.model.Inventory;
import com.warehousewms.model.PurchaseOrder;
import com.warehousewms.model.PurchaseOrderLine;
import com.warehousewms.model.Receipt;
import com.warehousewms.model.ReceiptLine;
import com.warehousewms.repository.AuditLogRepository;
import com.warehousewms.repository.InventoryRepository;
import com.warehousewms.repository.PurchaseOrderRepository;
import com.warehousewms.repository.ReceiptRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ReceivingService {
    private final DataSource dataSource;
    private final PurchaseOrderRepository poRepo;
    private final ReceiptRepository receiptRepo;
    private final InventoryRepository invRepo;
    private final AuditLogRepository auditRepo;

    public ReceivingService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.poRepo = new PurchaseOrderRepository(dataSource);
        this.receiptRepo = new ReceiptRepository(dataSource);
        this.invRepo = new InventoryRepository(dataSource);
        this.auditRepo = new AuditLogRepository(dataSource);
    }

    public void receivePO(int poId, List<ReceiptLine> receivedItems, String notes, int userId) throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            conn.setAutoCommit(false);

            PurchaseOrder po = poRepo.findById(poId);
            if (po == null) throw new SQLException("PO not found");

            Receipt r = new Receipt();
            r.setPoId(poId);
            r.setReceiptDate(new Date());
            r.setStatus("Completed");
            r.setNotes(notes);
            receiptRepo.insert(r, conn);

            List<PurchaseOrderLine> poLines = poRepo.findLinesByPoId(poId);

            for (ReceiptLine rl : receivedItems) {
                rl.setReceiptId(r.getReceiptId());
                receiptRepo.insertLine(rl, conn);

                for (PurchaseOrderLine pol : poLines) {
                    if (pol.getProductId() == rl.getProductId()) {
                        pol.setQuantityReceived(pol.getQuantityReceived() + rl.getQuantity());
                        poRepo.updateLine(pol, conn);
                        break;
                    }
                }

                Inventory inv = invRepo.findByProductAndBin(rl.getProductId(), rl.getBinId(), conn);
                int oldQty = 0;
                if (inv != null) {
                    oldQty = inv.getQuantity();
                    inv.setQuantity(oldQty + rl.getQuantity());
                    invRepo.update(inv, conn);
                } else {
                    inv = new Inventory();
                    inv.setProductId(rl.getProductId());
                    inv.setBinId(rl.getBinId());
                    inv.setQuantity(rl.getQuantity());
                    inv.setLotNumber(rl.getLotNumber());
                    inv.setExpiryDate(rl.getExpiryDate());
                    invRepo.insert(inv, conn);
                }
            }

            boolean allReceived = poLines.stream().allMatch(
                    pol -> pol.getQuantityReceived() >= pol.getQuantityOrdered());
            po.setStatus(allReceived ? "Closed" : "Open");
            poRepo.update(po, conn);

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }
}
