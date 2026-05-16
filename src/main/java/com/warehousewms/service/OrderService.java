package com.warehousewms.service;

import com.warehousewms.model.Order;
import com.warehousewms.model.OrderLine;
import com.warehousewms.repository.OrderRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class OrderService {
    private final OrderRepository orderRepo;
    private final DataSource dataSource;

    public OrderService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.orderRepo = new OrderRepository(dataSource);
    }

    public List<Order> getAllOrders() throws SQLException {
        return orderRepo.listAll();
    }

    public Order getOrderById(int orderId) throws SQLException {
        return orderRepo.findById(orderId);
    }

    public void createOrder(Order order, List<OrderLine> lines) throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            conn.setAutoCommit(false);
            orderRepo.insert(order, conn);
            for (OrderLine line : lines) {
                line.setOrderId(order.getOrderId());
                orderRepo.insertLine(line, conn);
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    public void updateOrder(Order order, List<OrderLine> lines) throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            conn.setAutoCommit(false);
            orderRepo.update(order, conn);
            for (OrderLine line : lines) {
                if (line.getOrderLineId() == 0) {
                    line.setOrderId(order.getOrderId());
                    orderRepo.insertLine(line, conn);
                } else {
                    orderRepo.updateLine(line, conn);
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

    public void deleteOrder(int orderId) throws SQLException {
        orderRepo.delete(orderId);
    }

    public List<OrderLine> getLinesForOrder(int orderId) throws SQLException {
        return orderRepo.findLinesByOrderId(orderId);
    }
}
