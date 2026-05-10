package com.warehousewms.model;

import java.util.Date;

public class Order {
    private int orderId;
    private int customerId;
    private Date orderDate;
    private Date shipByDate;
    private String status;
    private String notes;

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public Date getShipByDate() { return shipByDate; }
    public void setShipByDate(Date shipByDate) { this.shipByDate = shipByDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
