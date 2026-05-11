package com.warehousewms.service;

import com.warehousewms.config.ConnectionPool;
import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.*;
import com.warehousewms.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {
    private DataSource ds;
    private OrderService orderService;
    private int customerId;
    private int productId;

    @BeforeEach
    void setUp() throws SQLException {
        System.setProperty("wms.useSqlServer", "false");
        ds = new DatabaseManager().getDataSourceWithFallback();
        orderService = new OrderService(ds);

        CustomerRepository custRepo = new CustomerRepository(ds);
        Customer c = new Customer();
        c.setName("OrdCust" + System.nanoTime());
        custRepo.insert(c);
        customerId = custRepo.listAll().stream()
                .filter(x -> x.getName().equals(c.getName()))
                .findFirst().orElseThrow().getCustomerId();

        ProductRepository prodRepo = new ProductRepository(ds);
        Product p = new Product();
        p.setSku("ORD-" + System.nanoTime());
        p.setName("Order Product");
        prodRepo.insert(p);
        productId = prodRepo.listAll().stream()
                .filter(x -> x.getSku().equals(p.getSku()))
                .findFirst().orElseThrow().getProductId();
    }

    @AfterEach
    void tearDown() {
        ConnectionPool.shutdown();
        System.clearProperty("wms.useSqlServer");
    }

    @Test
    void createAndGetOrder() throws SQLException {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setOrderDate(new Date());
        order.setStatus("Pending");

        OrderLine line = new OrderLine();
        line.setProductId(productId);
        line.setQuantityOrdered(10);
        List<OrderLine> lines = new ArrayList<>();
        lines.add(line);

        orderService.createOrder(order, lines);
        assertTrue(order.getOrderId() > 0);

        Order found = orderService.getOrderById(order.getOrderId());
        assertNotNull(found);
        assertEquals("Pending", found.getStatus());

        List<OrderLine> fetched = orderService.getLinesForOrder(order.getOrderId());
        assertEquals(1, fetched.size());
        assertEquals(10, fetched.get(0).getQuantityOrdered());
    }

    @Test
    void getAllOrdersIncludesCreated() throws SQLException {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setOrderDate(new Date());
        order.setStatus("Pending");
        orderService.createOrder(order, new ArrayList<>());

        List<Order> all = orderService.getAllOrders();
        assertTrue(all.stream().anyMatch(o -> o.getOrderId() == order.getOrderId()));
    }
}
