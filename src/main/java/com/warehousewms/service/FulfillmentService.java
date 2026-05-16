package com.warehousewms.service;

import com.warehousewms.model.Inventory;
import com.warehousewms.model.Order;
import com.warehousewms.model.OrderLine;
import com.warehousewms.model.PickRun;
import com.warehousewms.model.PickRunItem;
import com.warehousewms.model.AuditLog;
import com.warehousewms.repository.AuditLogRepository;
import com.warehousewms.repository.InventoryRepository;
import com.warehousewms.repository.OrderRepository;
import com.warehousewms.repository.PickRunRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class FulfillmentService {
    private final DataSource dataSource;
    private final OrderRepository orderRepo;
    private final PickRunRepository pickRepo;
    private final InventoryRepository invRepo;
    private final AuditLogRepository auditRepo;

    public FulfillmentService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.orderRepo = new OrderRepository(dataSource);
        this.pickRepo = new PickRunRepository(dataSource);
        this.invRepo = new InventoryRepository(dataSource);
        this.auditRepo = new AuditLogRepository(dataSource);
    }

    public void createPickRun(int orderId, int assignedUserId, List<PickRunItem> itemsToPick) throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            conn.setAutoCommit(false);

            Order order = orderRepo.findById(orderId);
            if (order == null) throw new SQLException("Order not found");

            PickRun pr = new PickRun();
            pr.setAssignedToUserId(assignedUserId);
            pr.setStartedAt(new Date());
            pr.setStatus("InProgress");
            pickRepo.insert(pr, conn);

            for (PickRunItem item : itemsToPick) {
                item.setPickRunId(pr.getPickRunId());
                item.setStatus("Pending");
                pickRepo.insertItem(item, conn);
            }

            order.setStatus("Picking");
            orderRepo.update(order, conn);

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    public void completePick(int pickRunId, List<PickRunItem> pickedItems, int userId) throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            conn.setAutoCommit(false);

            PickRun pr = pickRepo.findById(pickRunId);
            if (pr == null) throw new SQLException("PickRun not found");

            pr.setCompletedAt(new Date());
            pr.setStatus("Completed");
            pickRepo.update(pr, conn);

            boolean allPicked = true;
            for (PickRunItem item : pickedItems) {
                item.setStatus("Picked");
                pickRepo.updateItem(item, conn);

                OrderLine line = orderRepo.findOrderLineById(item.getOrderLineId());
                if (line != null) {
                    Inventory inv = invRepo.findByProductAndBin(line.getProductId(), item.getBinId(), conn);
                    if (inv != null) {
                        int oldQty = inv.getQuantity();
                        int newQty = oldQty - item.getQuantityPicked();
                        if (newQty < 0) newQty = 0;
                        inv.setQuantity(newQty);
                        invRepo.update(inv, conn);
                    }
                    line.setQuantityPicked(line.getQuantityPicked() + item.getQuantityPicked());
                    orderRepo.updateLine(line, conn);
                }
            }

            List<PickRunItem> allItems = pickRepo.findItemsByPickRunId(pickRunId);
            for (PickRunItem i : allItems) {
                if (!"Picked".equals(i.getStatus())) {
                    allPicked = false;
                    break;
                }
            }

            if (allPicked && allItems.size() > 0) {
                OrderLine firstLine = orderRepo.findOrderLineById(allItems.get(0).getOrderLineId());
                if (firstLine != null) {
                    Order order = orderRepo.findById(firstLine.getOrderId());
                    if (order != null) {
                        order.setStatus("Picked");
                        orderRepo.update(order, conn);
                    }
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

    public List<PickRun> getAllPickRuns() throws SQLException {
        return pickRepo.listAll();
    }
}
