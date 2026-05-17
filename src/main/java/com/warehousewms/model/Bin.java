package com.warehousewms.model;

public class Bin {
    private int binId;
    private Integer parentBinId;
    private String name;
    private String binType;
    private double maxWeightKg;
    private double maxVolumeM3;
    private int sortOrder;

    public int getBinId() {
        return binId;
    }

    public void setBinId(int binId) {
        this.binId = binId;
    }

    public Integer getParentBinId() {
        return parentBinId;
    }

    public void setParentBinId(Integer parentBinId) {
        this.parentBinId = parentBinId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBinType() {
        return binType;
    }

    public void setBinType(String binType) {
        this.binType = binType;
    }

    public double getMaxWeightKg() {
        return maxWeightKg;
    }

    public void setMaxWeightKg(double maxWeightKg) {
        this.maxWeightKg = maxWeightKg;
    }

    public double getMaxVolumeM3() {
        return maxVolumeM3;
    }

    public void setMaxVolumeM3(double maxVolumeM3) {
        this.maxVolumeM3 = maxVolumeM3;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public String toString() {
        return binId + " - " + name + " [" + binType + "]";
    }
}

