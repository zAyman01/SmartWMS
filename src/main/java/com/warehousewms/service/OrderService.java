package com.warehousewms.service;

import com.warehousewms.model.Order;
import com.warehousewms.model.OrderLine;
import com.warehousewms.repository.OrderRepository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class OrderService {
    private final OrderRepository orderRepo;

    public OrderService(DataSource dataSource) {
        this.orderRepo = new OrderRepository(dataSource);
    }

    public List<Order> getAllOrders() throws SQLException {
        return orderRepo.listAll();
    }

    public Order getOrderById(int orderId) throws SQLException {
        return orderRepo.findById(orderId);
    }

    public void createOrder(Order order, List<OrderLine> lines) throws SQLException {
        orderRepo.insert(order);
        for (OrderLine line : lines) {
            line.setOrderId(order.getOrderId());
            orderRepo.insertLine(line);
        }
    }

    public void updateOrder(Order order, List<OrderLine> lines) throws SQLException {
        orderRepo.update(order);
        for (OrderLine line : lines) {
            if (line.getOrderLineId() == 0) {
                line.setOrderId(order.getOrderId());
                orderRepo.insertLine(line);
            } else {
                orderRepo.updateLine(line);
            }
        }
    }

    public List<OrderLine> getLinesForOrder(int orderId) throws SQLException {
        return orderRepo.findLinesByOrderId(orderId);
    }
}
