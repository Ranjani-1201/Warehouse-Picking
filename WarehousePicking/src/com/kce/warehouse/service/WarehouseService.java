package com.kce.warehouse.service;

import com.kce.warehouse.exception.BusinessException;
import com.kce.warehouse.model.*;
import com.kce.warehouse.util.IdGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class WarehouseService {
    private Map<String, Item> items = new HashMap<>();
    private Map<String, Location> locations = new HashMap<>();
    // key: itemId + "::" + locationId
    private Map<String, InventoryRecord> inventory = new HashMap<>();

    private Map<String, PickList> pickLists = new HashMap<>();
    private Map<String, Pack> packs = new HashMap<>();
    private Map<String, Shipment> shipments = new HashMap<>();

    // Add item
    public Item addItem(String name, String desc) {
        String id = IdGenerator.next("IT");
        Item it = new Item(id, name, desc);
        items.put(id, it);
        return it;
    }

    // Add location
    public Location addLocation(String name) {
        String id = IdGenerator.next("LOC");
        Location loc = new Location(id, name);
        locations.put(id, loc);
        return loc;
    }

    // Adjust inventory (increase or decrease)
    public InventoryRecord adjustInventory(String itemId, String locationId, int delta) throws BusinessException {
        Item item = items.get(itemId);
        if (item == null) throw new BusinessException("Item not found: " + itemId);
        Location loc = locations.get(locationId);
        if (loc == null) throw new BusinessException("Location not found: " + locationId);
        String key = key(itemId, locationId);
        InventoryRecord rec = inventory.get(key);
        if (rec == null) {
            if (delta < 0) throw new BusinessException("No inventory to reduce at location.");
            rec = new InventoryRecord(IdGenerator.next("IR"), item, loc, delta);
            inventory.put(key, rec);
        } else {
            int newQty = rec.getQuantity() + delta;
            if (newQty < 0) throw new BusinessException("Insufficient quantity at location.");
            rec.setQuantity(newQty);
        }
        return inventory.get(key);
    }

    // Create pick list - enforce sufficient on-hand (across locations)
    public PickList createPickList(Map<String, Integer> itemQtys) throws BusinessException {
        // validate total available across locations
        for (Map.Entry<String, Integer> e : itemQtys.entrySet()) {
            String itemId = e.getKey();
            int required = e.getValue();
            int available = totalAvailable(itemId);
            if (available < required) {
                throw new BusinessException("Insufficient total inventory for item " + itemId + " required=" + required + " available=" + available);
            }
        }
        // allocate tasks by greedily picking from locations with stock
        PickList pickList = new PickList(IdGenerator.next("PL"));
        for (Map.Entry<String, Integer> e : itemQtys.entrySet()) {
            String itemId = e.getKey();
            int remaining = e.getValue();
            // get locations sorted by quantity descending
            List<InventoryRecord> locs = inventory.values().stream()
                    .filter(r -> r.getItem().getItemId().equals(itemId) && r.getQuantity() > 0)
                    .sorted((a,b)-> Integer.compare(b.getQuantity(), a.getQuantity()))
                    .collect(Collectors.toList());
            for (InventoryRecord r : locs) {
                if (remaining <= 0) break;
                int take = Math.min(remaining, r.getQuantity());
                PickTask task = new PickTask(IdGenerator.next("PT"), r.getItem(), r.getLocation(), take);
                pickList.addTask(task);
                remaining -= take;
            }
            if (remaining > 0) {
                // should not happen because of prior validation
                throw new BusinessException("Allocation failed for item " + itemId);
            }
        }
        pickLists.put(pickList.getPickListId(), pickList);
        return pickList;
    }

    // Record pick for a specific pick task id and quantity actually picked.
    public PickTask recordPick(String pickListId, String pickTaskId, int qtyPicked) throws BusinessException {
        PickList pl = pickLists.get(pickListId);
        if (pl == null) throw new BusinessException("PickList not found: " + pickListId);
        Optional<PickTask> opt = pl.getTasks().stream().filter(t -> t.getTaskId().equals(pickTaskId)).findFirst();
        if (!opt.isPresent()) throw new BusinessException("PickTask not found: " + pickTaskId);
        PickTask pt = opt.get();
        // validation: cannot pick more than required
        if (qtyPicked < 0 || qtyPicked + pt.getPickedQty() > pt.getRequiredQty()) {
            throw new BusinessException("Invalid picked quantity. Required: " + pt.getRequiredQty() + " alreadyPicked: " + pt.getPickedQty());
        }
        // check inventory at location
        String key = key(pt.getItem().getItemId(), pt.getLocation().getLocationId());
        InventoryRecord rec = inventory.get(key);
        if (rec == null || rec.getQuantity() < qtyPicked) {
            throw new BusinessException("Not enough inventory at location to pick. Available: " + (rec==null?0:rec.getQuantity()));
        }
        // reduce available qty at location
        rec.setQuantity(rec.getQuantity() - qtyPicked);
        // update pick task
        pt.setPickedQty(pt.getPickedQty() + qtyPicked);
        pt.perform();
        pl.updateStatus();
        return pt;
    }

    // Create pack from pick list (pack only allowed if pick list exists and at least some picks done)
    public Pack createPack(String pickListId) throws BusinessException {
        PickList pl = pickLists.get(pickListId);
        if (pl == null) throw new BusinessException("PickList not found: " + pickListId);
        // confirm at least one task has been picked
        boolean anyPicked = pl.getTasks().stream().anyMatch(t -> t.getPickedQty() > 0);
        if (!anyPicked) throw new BusinessException("No picked quantities to pack.");
        Pack pack = new Pack(IdGenerator.next("PK"), pl);
        // record picked quantities into pack (we accept actual picked qty)
        for (PickTask t : pl.getTasks()) {
            if (t.getPickedQty() > 0) {
                pack.packItem(t.getItem(), t.getPickedQty());
            }
        }
        packs.put(pack.getPackId(), pack);
        return pack;
    }

    // Confirm pack (this step validates picked vs packed)
    public void confirmPack(String packId, Map<String, Integer> packingConfirmations) throws BusinessException {
        Pack pack = packs.get(packId);
        if (pack == null) throw new BusinessException("Pack not found: " + packId);
        // For each item in the pack, ensure the confirmed packed qty <= picked qty
        for (Map.Entry<String, Integer> e : packingConfirmations.entrySet()) {
            String itemId = e.getKey();
            int qty = e.getValue();
            int pickedQty = pack.getPickList().getTasks().stream()
                    .filter(t -> t.getItem().getItemId().equals(itemId))
                    .mapToInt(PickTask::getPickedQty).sum();
            if (qty > pickedQty) {
                throw new BusinessException("Confirmed packed qty greater than picked qty for item " + itemId);
            }
            // adjust pack internal representation to the confirmed qty
            pack.getPackedQuantities().put(itemId, qty);
        }
        pack.confirm();
    }

    // Create shipment for a pack (pack must be confirmed)
    public Shipment createShipment(String packId, String carrierName) throws BusinessException {
        Pack pack = packs.get(packId);
        if (pack == null) throw new BusinessException("Pack not found: " + packId);
        if (!"CONFIRMED".equals(pack.getStatus())) throw new BusinessException("Pack must be confirmed before shipping.");
        Shipment s = new Shipment(IdGenerator.next("SH"), pack, carrierName);
        shipments.put(s.getShipmentId(), s);
        return s;
    }

    // Close shipment — only if pack is confirmed
    public void closeShipment(String shipmentId) throws BusinessException {
        Shipment s = shipments.get(shipmentId);
        if (s == null) throw new BusinessException("Shipment not found: " + shipmentId);
        if (!"CONFIRMED".equals(s.getPack().getStatus())) throw new BusinessException("Pack not confirmed.");
        s.close();
    }

    // Inventory snapshot by item and location
    public void inventorySnapshot() {
        System.out.println("---- Inventory Snapshot ----");
        Map<String, List<InventoryRecord>> grouped = new HashMap<>();
        for (InventoryRecord r : inventory.values()) {
            grouped.computeIfAbsent(r.getItem().getItemId(), k -> new ArrayList<>()).add(r);
        }
        for (String itemId : grouped.keySet()) {
            Item it = items.get(itemId);
            System.out.println(it + ":");
            for (InventoryRecord r : grouped.get(itemId)) {
                System.out.println("   Location: " + r.getLocation() + " Qty: " + r.getQuantity());
            }
        }
        System.out.println("----------------------------");
    }

    // Show pick/pack summaries
    public void showPickListSummary(String pickListId) throws BusinessException {
        PickList pl = pickLists.get(pickListId);
        if (pl == null) throw new BusinessException("PickList not found: " + pickListId);
        System.out.println("PickList: " + pl.getPickListId() + " Status: " + pl.getStatus());
        for (PickTask t : pl.getTasks()) {
            System.out.println("  " + t);
        }
    }

    public void showPackSummary(String packId) throws BusinessException {
        Pack p = packs.get(packId);
        if (p == null) throw new BusinessException("Pack not found: " + packId);
        System.out.println("Pack: " + p.getPackId() + " Status: " + p.getStatus());
        // Show pick vs packed
        Map<String, Integer> pickedTotals = new HashMap<>();
        for (PickTask t : p.getPickList().getTasks()) {
            pickedTotals.put(t.getItem().getItemId(), pickedTotals.getOrDefault(t.getItem().getItemId(), 0) + t.getPickedQty());
        }
        System.out.println("ItemId | PickedQty | PackedQty | ItemName");
        for (String itemId : pickedTotals.keySet()) {
            int picked = pickedTotals.get(itemId);
            int packed = p.getPackedQtyForItem(itemId);
            System.out.println(itemId + " | " + picked + " | " + packed + " | " + items.get(itemId).getName());
            if (picked != packed) {
                System.out.println("   --> Discrepancy detected for " + itemId + " (picked " + picked + " vs packed " + packed + ")");
            }
        }
    }

    public void showShipmentManifest(String shipmentId) throws BusinessException {
        Shipment s = shipments.get(shipmentId);
        if (s == null) throw new BusinessException("Shipment not found: " + shipmentId);
        Pack p = s.getPack();
        System.out.println("Shipment: " + s.getShipmentId() + " Carrier: " + s.getCarrierName() + " Status: " + s.getStatus());
        System.out.println("Pack: " + p.getPackId());
        int total = 0;
        System.out.println("ItemId | Qty | ItemName");
        for (Map.Entry<String, Integer> e : p.getPackedQuantities().entrySet()) {
            String itemId = e.getKey();
            int qty = e.getValue();
            total += qty;
            System.out.println(itemId + " | " + qty + " | " + items.get(itemId).getName());
        }
        System.out.println("Total items in shipment: " + total);
    }

    // helpers
    private String key(String itemId, String locationId) {
        return itemId + "::" + locationId;
    }

    private int totalAvailable(String itemId) {
        return inventory.values().stream().filter(r -> r.getItem().getItemId().equals(itemId)).mapToInt(InventoryRecord::getQuantity).sum();
    }

    // Helpers to list items/locations and records
    public List<Item> listItems() {
        return new ArrayList<>(items.values());
    }

    public List<Location> listLocations() {
        return new ArrayList<>(locations.values());
    }

    public List<PickList> listPickLists() {
        return new ArrayList<>(pickLists.values());
    }

    public List<Pack> listPacks() {
        return new ArrayList<>(packs.values());
    }

    public List<Shipment> listShipments() {
        return new ArrayList<>(shipments.values());
    }
}
