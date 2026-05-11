package com.warehousewms.service;

import com.warehousewms.model.Order;
import com.warehousewms.model.OrderLine;
import com.warehousewms.model.PickRun;
import com.warehousewms.model.PickRunItem;
import com.warehousewms.repository.OrderRepository;
import com.warehousewms.repository.PickRunRepository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class FulfillmentService {
    private final OrderRepository orderRepo;
    private final PickRunRepository pickRepo;
    private final InventoryService inventoryService;

    public FulfillmentService(DataSource dataSource) {
        this.orderRepo = new OrderRepository(dataSource);
        this.pickRepo = new PickRunRepository(dataSource);
        this.inventoryService = new InventoryService(dataSource);
    }

    public void createPickRun(int orderId, int assignedUserId, List<PickRunItem> itemsToPick) throws SQLException {
        Order order = orderRepo.findById(orderId);
        if (order == null) throw new SQLException("Order not found");

        PickRun pr = new PickRun();
        pr.setAssignedToUserId(assignedUserId);
        pr.setStartedAt(new Date());
        pr.setStatus("InProgress");
        pickRepo.insert(pr);

        for (PickRunItem item : itemsToPick) {
            item.setPickRunId(pr.getPickRunId());
            item.setStatus("Pending");
            pickRepo.insertItem(item);
        }

        order.setStatus("Picking");
        orderRepo.update(order);
    }

    public void completePick(int pickRunId, List<PickRunItem> pickedItems, int userId) throws SQLException {
        PickRun pr = pickRepo.findById(pickRunId);
        if (pr == null) throw new SQLException("PickRun not found");

        pr.setCompletedAt(new Date());
        pr.setStatus("Completed");
        pickRepo.update(pr);

        for (PickRunItem item : pickedItems) {
            item.setStatus("Picked");
            pickRepo.updateItem(item);

            OrderLine line = orderRepo.findOrderLineById(item.getOrderLineId());
            if (line != null) {
                inventoryService.adjustStock(line.getProductId(), item.getBinId(), -item.getQuantityPicked(), userId, "PickRun " + pickRunId);
            }
        }

        // Ideally update Order status to Picked or Shipped
    }

    public List<PickRun> getAllPickRuns() throws SQLException {
        return pickRepo.listAll();
    }
}
