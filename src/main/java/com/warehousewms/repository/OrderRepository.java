package com.warehousewms.repository;

import com.warehousewms.model.Order;
import com.warehousewms.model.OrderLine;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private final DataSource dataSource;

    public OrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Order> listAll() throws SQLException {
        String sql = "SELECT OrderId, CustomerId, OrderDate, ShipByDate, Status, Notes FROM Orders ORDER BY OrderDate DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapOrder(rs));
            }
        }
        return list;
    }

    public Order findById(int orderId) throws SQLException {
        String sql = "SELECT OrderId, CustomerId, OrderDate, ShipByDate, Status, Notes FROM Orders WHERE OrderId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapOrder(rs);
            }
        }
        return null;
    }

    public void insert(Order order) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            insert(order, conn);
        }
    }

    public void insert(Order order, Connection conn) throws SQLException {
        String sql = "INSERT INTO Orders (CustomerId, OrderDate, ShipByDate, Status, Notes) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getCustomerId());
            ps.setTimestamp(2, new Timestamp(order.getOrderDate().getTime()));
            if (order.getShipByDate() != null) ps.setTimestamp(3, new Timestamp(order.getShipByDate().getTime()));
            else ps.setNull(3, Types.TIMESTAMP);
            ps.setString(4, order.getStatus());
            ps.setString(5, order.getNotes());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) order.setOrderId(rs.getInt(1));
        }
    }

    public void update(Order order) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            update(order, conn);
        }
    }

    public void update(Order order, Connection conn) throws SQLException {
        String sql = "UPDATE Orders SET CustomerId=?, OrderDate=?, ShipByDate=?, Status=?, Notes=? WHERE OrderId=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, order.getCustomerId());
            ps.setTimestamp(2, new Timestamp(order.getOrderDate().getTime()));
            if (order.getShipByDate() != null) ps.setTimestamp(3, new Timestamp(order.getShipByDate().getTime()));
            else ps.setNull(3, Types.TIMESTAMP);
            ps.setString(4, order.getStatus());
            ps.setString(5, order.getNotes());
            ps.setInt(6, order.getOrderId());
            ps.executeUpdate();
        }
    }

    public List<OrderLine> findLinesByOrderId(int orderId) throws SQLException {
        String sql = "SELECT OrderLineId, OrderId, ProductId, QuantityOrdered, QuantityPicked, QuantityShipped FROM OrderLines WHERE OrderId = ?";
        List<OrderLine> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapOrderLine(rs));
            }
        }
        return list;
    }

    public void insertLine(OrderLine line) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            insertLine(line, conn);
        }
    }

    public void insertLine(OrderLine line, Connection conn) throws SQLException {
        String sql = "INSERT INTO OrderLines (OrderId, ProductId, QuantityOrdered, QuantityPicked, QuantityShipped) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, line.getOrderId());
            ps.setInt(2, line.getProductId());
            ps.setInt(3, line.getQuantityOrdered());
            ps.setInt(4, line.getQuantityPicked());
            ps.setInt(5, line.getQuantityShipped());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) line.setOrderLineId(rs.getInt(1));
        }
    }

    public void updateLine(OrderLine line) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            updateLine(line, conn);
        }
    }

    public void updateLine(OrderLine line, Connection conn) throws SQLException {
        String sql = "UPDATE OrderLines SET OrderId=?, ProductId=?, QuantityOrdered=?, QuantityPicked=?, QuantityShipped=? WHERE OrderLineId=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, line.getOrderId());
            ps.setInt(2, line.getProductId());
            ps.setInt(3, line.getQuantityOrdered());
            ps.setInt(4, line.getQuantityPicked());
            ps.setInt(5, line.getQuantityShipped());
            ps.setInt(6, line.getOrderLineId());
            ps.executeUpdate();
        }
    }

    public void delete(int orderId) throws SQLException {
        String sqlLines = "DELETE FROM OrderLines WHERE OrderId=?";
        String sqlOrder = "DELETE FROM Orders WHERE OrderId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement psLines = conn.prepareStatement(sqlLines);
             PreparedStatement psOrder = conn.prepareStatement(sqlOrder)) {
            conn.setAutoCommit(false);
            try {
                psLines.setInt(1, orderId);
                psLines.executeUpdate();
                psOrder.setInt(1, orderId);
                psOrder.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public OrderLine findOrderLineById(int orderLineId) throws SQLException {
        String sql = "SELECT OrderLineId, OrderId, ProductId, QuantityOrdered, QuantityPicked, QuantityShipped FROM OrderLines WHERE OrderLineId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderLineId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapOrderLine(rs);
            }
        }
        return null;
    }

    public void deleteLine(int orderLineId) throws SQLException {
        String sql = "DELETE FROM OrderLines WHERE OrderLineId=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderLineId);
            ps.executeUpdate();
        }
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("OrderId"));
        o.setCustomerId(rs.getInt("CustomerId"));
        o.setOrderDate(rs.getTimestamp("OrderDate"));
        Timestamp shipBy = rs.getTimestamp("ShipByDate");
        if (shipBy != null) o.setShipByDate(new java.util.Date(shipBy.getTime()));
        o.setStatus(rs.getString("Status"));
        o.setNotes(rs.getString("Notes"));
        return o;
    }

    private OrderLine mapOrderLine(ResultSet rs) throws SQLException {
        OrderLine line = new OrderLine();
        line.setOrderLineId(rs.getInt("OrderLineId"));
        line.setOrderId(rs.getInt("OrderId"));
        line.setProductId(rs.getInt("ProductId"));
        line.setQuantityOrdered(rs.getInt("QuantityOrdered"));
        line.setQuantityPicked(rs.getInt("QuantityPicked"));
        line.setQuantityShipped(rs.getInt("QuantityShipped"));
        return line;
    }
}
