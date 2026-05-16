package com.warehousewms.service;

import com.warehousewms.model.PurchaseOrder;
import com.warehousewms.model.PurchaseOrderLine;
import com.warehousewms.repository.PurchaseOrderRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PurchaseOrderService {
    private final PurchaseOrderRepository poRepo;
    private final DataSource dataSource;

    public PurchaseOrderService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.poRepo = new PurchaseOrderRepository(dataSource);
    }

    public List<PurchaseOrder> getAllPOs() throws SQLException {
        return poRepo.listAll();
    }

    public PurchaseOrder getPOById(int poId) throws SQLException {
        return poRepo.findById(poId);
    }

    public void createPO(PurchaseOrder po, List<PurchaseOrderLine> lines) throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            conn.setAutoCommit(false);
            poRepo.insert(po, conn);
            for (PurchaseOrderLine line : lines) {
                line.setPoId(po.getPoId());
                poRepo.insertLine(line, conn);
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    public void updatePO(PurchaseOrder po, List<PurchaseOrderLine> lines) throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            conn.setAutoCommit(false);
            poRepo.update(po, conn);
            for (PurchaseOrderLine line : lines) {
                if (line.getPoLineId() == 0) {
                    line.setPoId(po.getPoId());
                    poRepo.insertLine(line, conn);
                } else {
                    poRepo.updateLine(line, conn);
                }
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    public void deletePO(int poId) throws SQLException {
        poRepo.delete(poId);
    }

    public List<PurchaseOrderLine> getLinesForPO(int poId) throws SQLException {
        return poRepo.findLinesByPoId(poId);
    }
}
