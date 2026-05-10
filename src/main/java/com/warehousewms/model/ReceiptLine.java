package com.warehousewms.model;

import java.util.Date;

public class ReceiptLine {
    private int receiptLineId;
    private int receiptId;
    private int productId;
    private int binId;
    private int quantity;
    private String lotNumber;
    private Date expiryDate;

    public int getReceiptLineId() { return receiptLineId; }
    public void setReceiptLineId(int receiptLineId) { this.receiptLineId = receiptLineId; }

    public int getReceiptId() { return receiptId; }
    public void setReceiptId(int receiptId) { this.receiptId = receiptId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getBinId() { return binId; }
    public void setBinId(int binId) { this.binId = binId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
}
