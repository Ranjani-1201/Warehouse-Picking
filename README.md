1.UML DIAGRAM
[umldiagram.pdf](https://github.com/user-attachments/files/22491650/umldiagram.pdf)

2.PROBLEM STATEMENT
Warehouse Picking, Packing & Shipping – Specification 
Document
Problem Statement:
Design and implement a Java console application for a Warehouse Operations 
system that manages items, locations, pick lists, packs, shipments, and carrier 
dispatches. The application should demonstrate object-oriented principles and 
maintain accurate inventory and fulfillment status.
Class Requirements:
1. Item
2. Location
3. InventoryRecord
4. PickList
5. PickTask
6. Pack
7. Shipment
Business Rules:
1. Pick lists can be created only if sufficient on-hand inventory exists at locations.
2. Picking reduces available quantity; packing confirms picked quantities.
3. Shipments can be closed only after packing completion.
4. Inventory records must reflect movements across locations.
5. Each pick task must link directly to an item and location.
Console Interface Requirements:
1. Menu-driven program: Add Item / Add Location / Adjust Inventory / Create Pick 
List / Record Pick / Create Pack / Ship Order / Inventory Summary / Exit
2. Input validations must be performed for all user entries.
3. Encapsulation must be followed for all attributes.
Expected Output Behavior:
• Show pick/pack summaries with quantities and discrepancies.
• Show shipment manifest with items and totals.
• Show inventory snapshot by item and location after each operation.

3.HOW TO RUN AND COMPILE
To run the Warehouse Operations system, first make sure the JDK is installed and configured on your machine. 
Place all .java files in the same project folder (maintaining package structure if used) and open a terminal in that location.
Compile the files using javac *.java or javac -d . *.java for packaged classes. Once compiled, execute the program with java MainApp,
which will launch the menu-driven console interface. From here, you can add items, adjust inventory, create pick lists, confirm packs,
ship orders, and view inventory summaries interactively.

4.OUTPUT SCREENSHOT
<img width="931" height="776" alt="image" src="https://github.com/user-attachments/assets/d992f65a-367c-45d0-adc9-6e5fc8f09636" />

RANJANI A [717823L343]
