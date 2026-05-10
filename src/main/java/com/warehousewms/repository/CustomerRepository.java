package com.warehousewms.repository;

import com.warehousewms.model.Customer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {
    private final DataSource dataSource;

    public CustomerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Customer findById(int customerId) throws SQLException {
        String sql = "SELECT CustomerId, Name, ContactName, Email, Phone FROM Customers WHERE CustomerId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapCustomer(rs);
            }
        }
        return null;
    }

    public List<Customer> listAll() throws SQLException {
        String sql = "SELECT CustomerId, Name, ContactName, Email, Phone FROM Customers ORDER BY Name";
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                customers.add(mapCustomer(rs));
            }
        }
        return customers;
    }

    public void insert(Customer customer) throws SQLException {
        String sql = "INSERT INTO Customers (Name, ContactName, Email, Phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getContactName());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhone());
            ps.executeUpdate();
        }
    }

    public void update(Customer customer) throws SQLException {
        String sql = "UPDATE Customers SET Name = ?, ContactName = ?, Email = ?, Phone = ? WHERE CustomerId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getContactName());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhone());
            ps.setInt(5, customer.getCustomerId());
            ps.executeUpdate();
        }
    }

    public boolean delete(int customerId) throws SQLException {
        String sql = "DELETE FROM Customers WHERE CustomerId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        }
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("CustomerId"));
        c.setName(rs.getString("Name"));
        c.setContactName(rs.getString("ContactName"));
        c.setEmail(rs.getString("Email"));
        c.setPhone(rs.getString("Phone"));
        return c;
    }
}
