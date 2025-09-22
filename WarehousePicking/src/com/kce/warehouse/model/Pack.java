package com.kce.warehouse.model;
import java.util.HashMap;
import java.util.Map;
public class Pack {
    private String packId;
    private PickList pickList;
    private Map<String, Integer> packedQuantities; 
    private String status; 
    public Pack(String packId, PickList pickList) {
        this.packId = packId;
        this.pickList = pickList;
        this.packedQuantities = new HashMap<>();
        this.status = "CREATED";
    }
    public String getPackId() {
        return packId;
    }
    public PickList getPickList() {
        return pickList;
    }
    public Map<String, Integer> getPackedQuantities() {
        return packedQuantities;
    }
    public void packItem(Item item, int qty) {
        packedQuantities.put(item.getItemId(), packedQuantities.getOrDefault(item.getItemId(), 0) + qty);
    }
    public int getPackedQtyForItem(String itemId) {
        return packedQuantities.getOrDefault(itemId, 0);
    }
    public void confirm() {
        status = "CONFIRMED";
    }
    public String getStatus() {
        return status;
    }
}
