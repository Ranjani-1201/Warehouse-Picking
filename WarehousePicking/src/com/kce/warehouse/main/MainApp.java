package com.kce.warehouse.main;
import com.kce.warehouse.exception.BusinessException;
import com.kce.warehouse.model.*;
import com.kce.warehouse.service.WarehouseService;
import java.util.*;
public class MainApp {
    private static WarehouseService svc = new WarehouseService();
    private static Scanner sc = new Scanner(System.in);
    public static void main(String[] args) {
        seedDemoData();
        boolean exit = false;
        while (!exit) {
            try {
                printMenu();
                int choice = readInt("Select option: ");
                switch (choice) {
                    case 1: addItem(); break;
                    case 2: addLocation(); break;
                    case 3: adjustInventory(); break;
                    case 4: createPickList(); break;
                    case 5: recordPick(); break;
                    case 6: createPack(); break;
                    case 7: shipOrder(); break;
                    case 8: inventorySummary(); break;
                    case 9: exit = true; System.out.println("Bye!"); break;
                    default: System.out.println("Invalid choice.");
                }
            } catch (BusinessException be) {
                System.out.println("Business error: " + be.getMessage());
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        } }
 private static void printMenu() {
        System.out.println("\n=== Warehouse Operations ===");
        System.out.println("1. Add Item");
        System.out.println("2. Add Location");
        System.out.println("3. Adjust Inventory");
        System.out.println("4. Create Pick List");
        System.out.println("5. Record Pick");
        System.out.println("6. Create Pack");
        System.out.println("7. Ship Order");
        System.out.println("8. Inventory Summary");
        System.out.println("9. Exit");
    }
 private static void addItem() {
        System.out.println("Add Item");
        String name = readString("Name: ");
        String desc = readString("Description: ");
        Item it = svc.addItem(name, desc);
        System.out.println("Added item: " + it);
    }  private static void addLocation() {
        System.out.println("Add Location");
        String name = readString("Location name: ");
        Location loc = svc.addLocation(name);
        System.out.println("Added location: " + loc);
    }
private static void adjustInventory() throws BusinessException {
        System.out.println("Adjust Inventory");
        listItems();
        String itemId = readString("ItemId: ");
        listLocations();
        String locId = readString("LocationId: ");
        int delta = readInt("Delta qty (use negative to reduce): ");
        InventoryRecord rec = svc.adjustInventory(itemId, locId, delta);
        System.out.println("Inventory updated: Item " + rec.getItem() + " Location " + rec.getLocation() + " Qty " + rec.getQuantity());
        svc.inventorySnapshot();
    }
 private static void createPickList() throws BusinessException {
        System.out.println("Create Pick List");
        Map<String, Integer> req = new HashMap<>();
        while (true) {
            listItems();
            String itemId = readString("ItemId (or 'done'): ");
            if ("done".equalsIgnoreCase(itemId)) break;
            int qty = readInt("Qty required: ");
            if (qty <= 0) {
                System.out.println("Qty must be positive.");
                continue;
            }
            req.put(itemId, req.getOrDefault(itemId, 0) + qty);
        }
        if (req.isEmpty()) {
            System.out.println("No items provided.");
            return;
        }
        PickList pl = svc.createPickList(req);
        System.out.println("Created PickList: " + pl.getPickListId());
        svc.showPickListSummary(pl.getPickListId());
        svc.inventorySnapshot();
    }
 private static void recordPick() throws BusinessException {
        System.out.println("Record Pick");
        listPickLists();
        String pickListId = readString("PickListId: ");
        svc.showPickListSummary(pickListId);
        String pickTaskId = readString("PickTaskId: ");
        int qty = readInt("Qty actually picked: ");
        PickTask pt = svc.recordPick(pickListId, pickTaskId, qty);
        System.out.println("Updated pick task: " + pt);
        svc.showPickListSummary(pickListId);
        svc.inventorySnapshot();
    }
 private static void createPack() throws BusinessException {
        System.out.println("Create Pack");
        listPickLists();
        String pickListId = readString("PickListId: ");
        Pack pack = svc.createPack(pickListId);
        System.out.println("Created Pack: " + pack.getPackId());
        svc.showPackSummary(pack.getPackId());
        Map<String, Integer> confirmations = new HashMap<>();
        System.out.println("Confirm packed quantities for the above items (or type 'skip' to accept picked quantities):");
        for (PickTask t : pack.getPickList().getTasks()) {
            if (t.getPickedQty() <= 0) continue;
            String itemId = t.getItem().getItemId();
            String resp = readString("Item " + itemId + " picked " + t.getPickedQty() + " -> confirm qty (enter number or 'skip'): ");
            if ("skip".equalsIgnoreCase(resp)) {
                confirmations.put(itemId, t.getPickedQty());
            } else {
                try {
                    int v = Integer.parseInt(resp);
                    confirmations.put(itemId, v);
                } catch (NumberFormatException nfe) {
                    System.out.println("Invalid input, using picked qty.");
                    confirmations.put(itemId, t.getPickedQty());
                }
            }
        }
        svc.confirmPack(pack.getPackId(), confirmations);
        System.out.println("Pack confirmed.");
        svc.showPackSummary(pack.getPackId());
    }
 private static void shipOrder() throws BusinessException {
        System.out.println("Ship Order");
        listPacks();
        String packId = readString("PackId: ");
        String carrier = readString("Carrier name: ");
        Shipment s = svc.createShipment(packId, carrier);
        System.out.println("Created Shipment: " + s.getShipmentId());
        svc.showShipmentManifest(s.getShipmentId());

        String close = readString("Close shipment now? (y/n): ");
        if ("y".equalsIgnoreCase(close)) {
            svc.closeShipment(s.getShipmentId());
            System.out.println("Shipment closed.");
            svc.showShipmentManifest(s.getShipmentId());
        }
    }
 private static void inventorySummary() {
        svc.inventorySnapshot();
    }
 private static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }
 private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String l = sc.nextLine().trim();
            try {
                return Integer.parseInt(l);
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid integer.");
            }
        }
    }
 private static void listItems() {
        System.out.println("Items:");
        for (Item it : svc.listItems()) {
            System.out.println("   " + it.getItemId() + " - " + it.getName());
        }
    }
 private static void listLocations() {
        System.out.println("Locations:");
        for (Location loc : svc.listLocations()) {
            System.out.println("   " + loc.getLocationId() + " - " + loc.getName());
        }
    }
 private static void listPickLists() {
        System.out.println("PickLists:");
        for (PickList pl : svc.listPickLists()) {
            System.out.println("   " + pl.getPickListId() + " Status: " + pl.getStatus());
        }
    }
private static void listPacks() {
        System.out.println("Packs:");
        for (Pack p : svc.listPacks()) {
            System.out.println("   " + p.getPackId() + " For PickList: " + p.getPickList().getPickListId() + " Status: " + p.getStatus());
        }
    }
private static void seedDemoData() {
        System.out.println("Seeding sample data...");
        Item a = svc.addItem("Widget-A", "Small widget");
        Item b = svc.addItem("Widget-B", "Large widget");
        Location l1 = svc.addLocation("Aisle-1");
        Location l2 = svc.addLocation("Aisle-2");
        try {
            svc.adjustInventory(a.getItemId(), l1.getLocationId(), 50);
            svc.adjustInventory(a.getItemId(), l2.getLocationId(), 20);
            svc.adjustInventory(b.getItemId(), l1.getLocationId(), 10);
        } catch (BusinessException e) {
            System.out.println("Seeding error: " + e.getMessage());
        }
        svc.inventorySnapshot();
    }
}

