package com.warehousewms.model;

public class Product {
    private int productId;
    private String sku;
    private String name;
    private String imagePath;
    private double unitWeightKg;
    private double unitVolumeM3;
    private boolean isActive = true;
    private String barcode;

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public double getUnitWeightKg() { return unitWeightKg; }
    public void setUnitWeightKg(double unitWeightKg) { this.unitWeightKg = unitWeightKg; }

    public double getUnitVolumeM3() { return unitVolumeM3; }
    public void setUnitVolumeM3(double unitVolumeM3) { this.unitVolumeM3 = unitVolumeM3; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
}

