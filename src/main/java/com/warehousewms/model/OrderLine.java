package com.warehousewms.model;

public class OrderLine {
    private int orderLineId;
    private int orderId;
    private int productId;
    private int quantityOrdered;
    private int quantityPicked;
    private int quantityShipped;

    public int getOrderLineId() { return orderLineId; }
    public void setOrderLineId(int orderLineId) { this.orderLineId = orderLineId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(int quantityOrdered) { this.quantityOrdered = quantityOrdered; }

    public int getQuantityPicked() { return quantityPicked; }
    public void setQuantityPicked(int quantityPicked) { this.quantityPicked = quantityPicked; }

    public int getQuantityShipped() { return quantityShipped; }
    public void setQuantityShipped(int quantityShipped) { this.quantityShipped = quantityShipped; }
}
