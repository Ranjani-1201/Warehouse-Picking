package com.kce.warehouse.model;
public class InventoryRecord {
    private String recordId;
    private Item item;
    private Location location;
    private int quantity;
 public InventoryRecord(String recordId, Item item, Location location, int quantity) {
        this.recordId = recordId;
        this.item = item;
        this.location = location;
        this.quantity = quantity;
    }
public String getRecordId() {
        return recordId;
    }
 public Item getItem() {
        return item;
    }
public Location getLocation() {
        return location;
    }
public int getQuantity() {
        return quantity;
    }
public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

