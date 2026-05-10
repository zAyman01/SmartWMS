package com.warehousewms.service;

import com.warehousewms.model.PurchaseOrder;
import com.warehousewms.model.PurchaseOrderLine;
import com.warehousewms.repository.PurchaseOrderRepository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class PurchaseOrderService {
    private final PurchaseOrderRepository poRepo;

    public PurchaseOrderService(DataSource dataSource) {
        this.poRepo = new PurchaseOrderRepository(dataSource);
    }

    public List<PurchaseOrder> getAllPOs() throws SQLException {
        return poRepo.listAll();
    }

    public PurchaseOrder getPOById(int poId) throws SQLException {
        return poRepo.findById(poId);
    }

    public void createPO(PurchaseOrder po, List<PurchaseOrderLine> lines) throws SQLException {
        poRepo.insert(po);
        for (PurchaseOrderLine line : lines) {
            line.setPoId(po.getPoId());
            poRepo.insertLine(line);
        }
    }

    public void updatePO(PurchaseOrder po, List<PurchaseOrderLine> lines) throws SQLException {
        poRepo.update(po);
        // Note: Full line update logic would handle diffing. 
        // For simplicity, we assume we might update lines individually or just re-insert.
        // Or if they exist, update them.
        for (PurchaseOrderLine line : lines) {
            if (line.getPoLineId() == 0) {
                line.setPoId(po.getPoId());
                poRepo.insertLine(line);
            } else {
                poRepo.updateLine(line);
            }
        }
    }

    public List<PurchaseOrderLine> getLinesForPO(int poId) throws SQLException {
        return poRepo.findLinesByPoId(poId);
    }
}
