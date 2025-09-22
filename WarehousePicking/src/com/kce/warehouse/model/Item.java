package com.kce.warehouse.model;
public class Item {
    private String itemId;
    private String name;
    private String description;
    public Item(String itemId, String name, String description) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
    }
 public String getItemId() {
        return itemId;
    }
 public void setItemId(String itemId) {
        this.itemId = itemId;
    }
 public String getName() {
        return name;
    }
public void setName(String name) {
        this.name = name;
    }
 public String getDescription() {
        return description;
    }
 public void setDescription(String description) {
        this.description = description;
    }
 @Override
    public String toString() {
        return name + " (" + itemId + ")";
    }
}
