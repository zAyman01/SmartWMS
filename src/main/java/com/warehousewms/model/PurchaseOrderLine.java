package com.warehousewms.model;

public class PurchaseOrderLine {
    private int poLineId;
    private int poId;
    private int productId;
    private int quantityOrdered;
    private int quantityReceived;

    public int getPoLineId() { return poLineId; }
    public void setPoLineId(int poLineId) { this.poLineId = poLineId; }

    public int getPoId() { return poId; }
    public void setPoId(int poId) { this.poId = poId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(int quantityOrdered) { this.quantityOrdered = quantityOrdered; }

    public int getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(int quantityReceived) { this.quantityReceived = quantityReceived; }
}
