package com.warehousewms.model;

import java.util.Date;

public class Receipt {
    private int receiptId;
    private Integer poId;
    private Date receiptDate;
    private String status;
    private String notes;

    public int getReceiptId() { return receiptId; }
    public void setReceiptId(int receiptId) { this.receiptId = receiptId; }

    public Integer getPoId() { return poId; }
    public void setPoId(Integer poId) { this.poId = poId; }

    public Date getReceiptDate() { return receiptDate; }
    public void setReceiptDate(Date receiptDate) { this.receiptDate = receiptDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
