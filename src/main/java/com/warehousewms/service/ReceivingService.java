package com.warehousewms.service;

import com.warehousewms.model.PurchaseOrder;
import com.warehousewms.model.PurchaseOrderLine;
import com.warehousewms.model.Receipt;
import com.warehousewms.model.ReceiptLine;
import com.warehousewms.repository.PurchaseOrderRepository;
import com.warehousewms.repository.ReceiptRepository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ReceivingService {
    private final PurchaseOrderRepository poRepo;
    private final ReceiptRepository receiptRepo;
    private final InventoryService inventoryService;

    public ReceivingService(DataSource dataSource) {
        this.poRepo = new PurchaseOrderRepository(dataSource);
        this.receiptRepo = new ReceiptRepository(dataSource);
        this.inventoryService = new InventoryService(dataSource);
    }

    public void receivePO(int poId, List<ReceiptLine> receivedItems, String notes, int userId) throws SQLException {
        PurchaseOrder po = poRepo.findById(poId);
        if (po == null) throw new SQLException("PO not found");

        // Create Receipt
        Receipt r = new Receipt();
        r.setPoId(poId);
        r.setReceiptDate(new Date());
        r.setStatus("Completed");
        r.setNotes(notes);
        receiptRepo.insert(r);

        // Process lines
        List<PurchaseOrderLine> poLines = poRepo.findLinesByPoId(poId);

        for (ReceiptLine rl : receivedItems) {
            rl.setReceiptId(r.getReceiptId());
            receiptRepo.insertLine(rl);

            // Update PO Line Quantity Received
            for (PurchaseOrderLine pol : poLines) {
                if (pol.getProductId() == rl.getProductId()) {
                    pol.setQuantityReceived(pol.getQuantityReceived() + rl.getQuantity());
                    poRepo.updateLine(pol);
                    break;
                }
            }

            // Add stock via InventoryService
            inventoryService.adjustStock(rl.getProductId(), rl.getBinId(), rl.getQuantity(), userId, "PO Receipt " + r.getReceiptId());
        }

        // Update PO status if fully received (simplified logic)
        po.setStatus("Closed");
        poRepo.update(po);
    }
}
