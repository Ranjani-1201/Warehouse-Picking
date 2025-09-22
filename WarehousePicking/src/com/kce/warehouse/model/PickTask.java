package com.kce.warehouse.model;

public class PickTask extends Task {
    private Item item;
    private Location location;
    private int requiredQty;
    private int pickedQty;

    public PickTask(String taskId, Item item, Location location, int requiredQty) {
        super(taskId);
        this.item = item;
        this.location = location;
        this.requiredQty = requiredQty;
        this.pickedQty = 0;
    }

    @Override
    public boolean perform() {
        // perform here is domain-specific; the actual pick action is handled by service.
        // We'll mark IN_PROGRESS when pick begins, COMPLETED when pickedQty == requiredQty
        if ("CREATED".equals(status)) status = "IN_PROGRESS";
        if (pickedQty >= requiredQty) {
            status = "COMPLETED";
            return true;
        }
        return false;
    }

    public Item getItem() {
        return item;
    }

    public Location getLocation() {
        return location;
    }

    public int getRequiredQty() {
        return requiredQty;
    }

    public int getPickedQty() {
        return pickedQty;
    }

    public void setPickedQty(int pickedQty) {
        this.pickedQty = pickedQty;
        if (this.pickedQty >= requiredQty) {
            this.pickedQty = requiredQty;
            status = "COMPLETED";
        } else if (this.pickedQty > 0) {
            status = "IN_PROGRESS";
        }
    }

    @Override
    public String toString() {
        return "PickTask[" + taskId + "] item=" + item + " loc=" + location + " req=" + requiredQty + " picked=" + pickedQty + " status=" + status;
    }
}

