package com.warehousewms.model;

public class PickRunItem {
    private int pickRunItemId;
    private int pickRunId;
    private int orderLineId;
    private int binId;
    private int quantityToPick;
    private int quantityPicked;
    private String status;

    public int getPickRunItemId() { return pickRunItemId; }
    public void setPickRunItemId(int pickRunItemId) { this.pickRunItemId = pickRunItemId; }

    public int getPickRunId() { return pickRunId; }
    public void setPickRunId(int pickRunId) { this.pickRunId = pickRunId; }

    public int getOrderLineId() { return orderLineId; }
    public void setOrderLineId(int orderLineId) { this.orderLineId = orderLineId; }

    public int getBinId() { return binId; }
    public void setBinId(int binId) { this.binId = binId; }

    public int getQuantityToPick() { return quantityToPick; }
    public void setQuantityToPick(int quantityToPick) { this.quantityToPick = quantityToPick; }

    public int getQuantityPicked() { return quantityPicked; }
    public void setQuantityPicked(int quantityPicked) { this.quantityPicked = quantityPicked; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
