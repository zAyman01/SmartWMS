package com.warehousewms;

import com.warehousewms.model.*;
import com.warehousewms.repository.*;
import com.warehousewms.util.SessionContext;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

public class SeedData {
    private final DataSource dataSource;

    private final UserRepository userRepo;
    private final SupplierRepository supplierRepo;
    private final ProductRepository productRepo;
    private final CustomerRepository customerRepo;
    private final BinRepository binRepo;
    private final InventoryRepository invRepo;
    private final PurchaseOrderRepository poRepo;
    private final OrderRepository orderRepo;
    private final ReceiptRepository receiptRepo;
    private final PickRunRepository pickRepo;

    public SeedData(DataSource dataSource) {
        this.dataSource = dataSource;
        this.userRepo = new UserRepository(dataSource);
        this.supplierRepo = new SupplierRepository(dataSource);
        this.productRepo = new ProductRepository(dataSource);
        this.customerRepo = new CustomerRepository(dataSource);
        this.binRepo = new BinRepository(dataSource);
        this.invRepo = new InventoryRepository(dataSource);
        this.poRepo = new PurchaseOrderRepository(dataSource);
        this.orderRepo = new OrderRepository(dataSource);
        this.receiptRepo = new ReceiptRepository(dataSource);
        this.pickRepo = new PickRunRepository(dataSource);
    }

    public void seedAll() throws SQLException {
        if (userRepo.listUsers().size() > 1) return;

        seedUsers();
        seedSuppliers();
        seedProducts();
        seedCustomers();
        seedBins();
        seedInventory();
        seedPurchaseOrders();
        seedCustomerOrders();
    }

    private void seedUsers() throws SQLException {
        User u = makeUser("alice", "Alice Johnson", "Supervisor");
        userRepo.insertUser(u);
        u = makeUser("bob", "Bob Martinez", "Picker");
        userRepo.insertUser(u);
        u = makeUser("carol", "Carol Chen", "Picker");
        userRepo.insertUser(u);
        u = makeUser("dave", "Dave Wilson", "Operator");
        userRepo.insertUser(u);
    }

    private User makeUser(String username, String fullName, String role) {
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(username + "123");
        u.setFullName(fullName);
        u.setRole(role);
        return u;
    }

    private void seedSuppliers() throws SQLException {
        supplierRepo.insert(makeSupplier("TechParts Inc.", "Mark Lee", "mark@techparts.com", "+1-555-0101"));
        supplierRepo.insert(makeSupplier("BoxMakers Ltd.", "Sarah Connor", "sarah@boxmakers.com", "+1-555-0102"));
        supplierRepo.insert(makeSupplier("Global Logistics Co.", "Tom Hardy", "tom@glogistics.com", "+1-555-0103"));
        supplierRepo.insert(makeSupplier("FreshSupply GmbH", "Eva Braun", "eva@freshsupply.de", "+49-30-1234"));
        supplierRepo.insert(makeSupplier("SafetyFirst Corp.", "John Rambo", "john@safetyfirst.com", "+1-555-0104"));
    }

    private Supplier makeSupplier(String name, String contact, String email, String phone) {
        Supplier s = new Supplier();
        s.setName(name);
        s.setContactName(contact);
        s.setEmail(email);
        s.setPhone(phone);
        return s;
    }

    private void seedProducts() throws SQLException {
        productRepo.insert(makeProduct("ELEC-001", "Wireless Mouse", 0.15, 0.0005, "4901234567890"));
        productRepo.insert(makeProduct("ELEC-002", "Mechanical Keyboard", 0.85, 0.0020, "4901234567891"));
        productRepo.insert(makeProduct("ELEC-003", "USB-C Hub 7-in-1", 0.12, 0.0003, "4901234567892"));
        productRepo.insert(makeProduct("ELEC-004", "27\" IPS Monitor", 5.20, 0.0350, "4901234567893"));
        productRepo.insert(makeProduct("ELEC-005", "Noise Cancelling Headphones", 0.25, 0.0015, "4901234567894"));
        productRepo.insert(makeProduct("PACK-001", "Cardboard Box 40x30x20", 0.35, 0.0240, "5901234567890"));
        productRepo.insert(makeProduct("PACK-002", "Bubble Wrap Roll 50m", 0.80, 0.0080, "5901234567891"));
        productRepo.insert(makeProduct("PACK-003", "Packing Tape 48mmx100m", 0.20, 0.0006, "5901234567892"));
        productRepo.insert(makeProduct("CHEM-001", "Industrial Lubricant 5L", 4.50, 0.0052, "6901234567890"));
        productRepo.insert(makeProduct("CHEM-002", "Cleaning Solvent 1L", 0.95, 0.0011, "6901234567891"));
        productRepo.insert(makeProduct("SAFE-001", "Safety Goggles (Pack of 10)", 0.30, 0.0018, "7901234567890"));
        productRepo.insert(makeProduct("SAFE-002", "Nitrile Gloves Box (100 pcs)", 0.45, 0.0025, "7901234567891"));
        productRepo.insert(makeProduct("SAFE-003", "Hard Hat - Yellow", 0.38, 0.0030, "7901234567892"));
        productRepo.insert(makeProduct("FOOD-001", "Spring Water 24x500ml", 12.00, 0.0290, "8901234567890"));
        productRepo.insert(makeProduct("FOOD-002", "Energy Bars (Case of 24)", 3.60, 0.0120, "8901234567891"));
    }

    private Product makeProduct(String sku, String name, double weight, double volume, String barcode) {
        Product p = new Product();
        p.setSku(sku);
        p.setName(name);
        p.setUnitWeightKg(weight);
        p.setUnitVolumeM3(volume);
        p.setActive(true);
        p.setBarcode(barcode);
        return p;
    }

    private void seedCustomers() throws SQLException {
        customerRepo.insert(makeCustomer("Acme Corp.", "Wile E. Coyote", "orders@acme.com", "+1-555-0201"));
        customerRepo.insert(makeCustomer("Globex Industries", "Hank Scorpio", "hank@globex.com", "+1-555-0202"));
        customerRepo.insert(makeCustomer("Initech", "Bill Lumbergh", "bill@initech.com", "+1-555-0203"));
        customerRepo.insert(makeCustomer("Oceanic Airlines", "Jack Shephard", "cargo@oceanic.com", "+1-555-0204"));
        customerRepo.insert(makeCustomer("Stark Enterprises", "Tony Stark", "logistics@stark.com", "+1-555-0205"));
    }

    private Customer makeCustomer(String name, String contact, String email, String phone) {
        Customer c = new Customer();
        c.setName(name);
        c.setContactName(contact);
        c.setEmail(email);
        c.setPhone(phone);
        return c;
    }

    private void seedBins() throws SQLException {
        Bin zoneA = new Bin(); zoneA.setName("Zone A"); zoneA.setBinType("Zone"); zoneA.setSortOrder(1);
        binRepo.insert(zoneA);
        Bin zoneB = new Bin(); zoneB.setName("Zone B"); zoneB.setBinType("Zone"); zoneB.setSortOrder(2);
        binRepo.insert(zoneB);
        Bin zoneC = new Bin(); zoneC.setName("Zone C"); zoneC.setBinType("Zone"); zoneC.setSortOrder(3);
        binRepo.insert(zoneC);

        makeAisle(zoneA, "A-Aisle-1", 10, 1000.0, 5.0);
        makeAisle(zoneA, "A-Aisle-2", 20, 1000.0, 5.0);
        makeAisle(zoneB, "B-Aisle-1", 30, 1500.0, 8.0);
        makeAisle(zoneB, "B-Aisle-2", 40, 1500.0, 8.0);
        makeAisle(zoneC, "C-Aisle-1", 50, 800.0, 3.5);
    }

    private void makeAisle(Bin zone, String name, int sortOrder, double maxW, double maxV) throws SQLException {
        Bin aisle = new Bin();
        aisle.setName(name);
        aisle.setBinType("Aisle");
        aisle.setParentBinId(zone.getBinId());
        aisle.setSortOrder(sortOrder);
        aisle.setMaxWeightKg(maxW);
        aisle.setMaxVolumeM3(maxV);
        binRepo.insert(aisle);

        for (int r = 1; r <= 3; r++) {
            makeRack(aisle, name + "-Rack-" + r, sortOrder + r, maxW / 3, maxV / 3);
        }
    }

    private void makeRack(Bin aisle, String name, int sortOrder, double maxW, double maxV) throws SQLException {
        Bin rack = new Bin();
        rack.setName(name);
        rack.setBinType("Rack");
        rack.setParentBinId(aisle.getBinId());
        rack.setSortOrder(sortOrder);
        rack.setMaxWeightKg(maxW);
        rack.setMaxVolumeM3(maxV);
        binRepo.insert(rack);

        for (int s = 1; s <= 2; s++) {
            makeLocation(rack, name + "-Shelf-" + s, sortOrder + s * 10, maxW / 2, maxV / 2);
        }
    }

    private void makeLocation(Bin rack, String name, int sortOrder, double maxW, double maxV) throws SQLException {
        Bin loc = new Bin();
        loc.setName(name);
        loc.setBinType("Location");
        loc.setParentBinId(rack.getBinId());
        loc.setSortOrder(sortOrder);
        loc.setMaxWeightKg(maxW);
        loc.setMaxVolumeM3(maxV);
        binRepo.insert(loc);
    }

    private void seedInventory() throws SQLException {
        addInventory(1, findBin("A-Aisle-1-Rack-1-Shelf-1"), 150, "LOT-2026-001", daysFromNow(365));
        addInventory(2, findBin("A-Aisle-1-Rack-1-Shelf-1"), 80, "LOT-2026-001", daysFromNow(365));
        addInventory(3, findBin("A-Aisle-1-Rack-1-Shelf-2"), 200, "LOT-2026-002", daysFromNow(180));
        addInventory(5, findBin("A-Aisle-1-Rack-2-Shelf-1"), 60, "LOT-2026-003", daysFromNow(240));
        addInventory(6, findBin("A-Aisle-2-Rack-1-Shelf-1"), 500, "LOT-2026-004", null);
        addInventory(7, findBin("A-Aisle-2-Rack-1-Shelf-2"), 300, "LOT-2026-004", null);
        addInventory(8, findBin("A-Aisle-2-Rack-2-Shelf-1"), 1000, "LOT-2026-005", null);
        addInventory(4, findBin("B-Aisle-1-Rack-1-Shelf-1"), 25, "LOT-2026-006", daysFromNow(500));
        addInventory(9, findBin("B-Aisle-1-Rack-1-Shelf-2"), 40, "LOT-2026-007", daysFromNow(730));
        addInventory(10, findBin("B-Aisle-2-Rack-1-Shelf-1"), 120, "LOT-2026-008", daysFromNow(365));
        addInventory(11, findBin("C-Aisle-1-Rack-1-Shelf-1"), 90, "LOT-2026-009", null);
        addInventory(12, findBin("C-Aisle-1-Rack-1-Shelf-2"), 200, "LOT-2026-010", null);
        addInventory(13, findBin("C-Aisle-1-Rack-2-Shelf-1"), 50, "LOT-2026-011", null);
        addInventory(14, findBin("C-Aisle-1-Rack-2-Shelf-2"), 80, "LOT-2026-012", daysFromNow(120));
        addInventory(15, findBin("C-Aisle-1-Rack-3-Shelf-1"), 45, "LOT-2026-013", daysFromNow(200));
    }

    private void addInventory(int productId, int binId, int qty, String lot, Date expiry) throws SQLException {
        Inventory inv = new Inventory();
        inv.setProductId(productId);
        inv.setBinId(binId);
        inv.setQuantity(qty);
        inv.setLotNumber(lot);
        inv.setExpiryDate(expiry);
        invRepo.insert(inv);
    }

    private int findBin(String name) throws SQLException {
        return binRepo.listAll().stream()
                .filter(b -> b.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Bin not found: " + name))
                .getBinId();
    }

    private void seedPurchaseOrders() throws SQLException {
        // PO-1: Closed (fully received)
        PurchaseOrder po1 = new PurchaseOrder();
        po1.setSupplierId(1);
        po1.setOrderDate(daysAgo(14));
        po1.setStatus("Closed");
        po1.setNotes("Monthly electronics restock");
        poRepo.insert(po1);
        insertPOLine(po1.getPoId(), 1, 100, 100);
        insertPOLine(po1.getPoId(), 2, 50, 50);
        insertPOLine(po1.getPoId(), 5, 30, 30);

        // PO-2: Open (not yet received)
        PurchaseOrder po2 = new PurchaseOrder();
        po2.setSupplierId(2);
        po2.setOrderDate(daysAgo(3));
        po2.setStatus("Open");
        po2.setNotes("Packaging supplies - urgent");
        poRepo.insert(po2);
        insertPOLine(po2.getPoId(), 6, 200, 0);
        insertPOLine(po2.getPoId(), 7, 150, 0);
        insertPOLine(po2.getPoId(), 8, 500, 0);

        // PO-3: Open with partial receipt
        PurchaseOrder po3 = new PurchaseOrder();
        po3.setSupplierId(3);
        po3.setOrderDate(daysAgo(7));
        po3.setStatus("Open");
        po3.setNotes("Safety equipment");
        poRepo.insert(po3);
        insertPOLine(po3.getPoId(), 11, 100, 50);
        insertPOLine(po3.getPoId(), 12, 200, 0);
        insertPOLine(po3.getPoId(), 13, 75, 0);

        // PO-4: Closed from FreshSupply
        PurchaseOrder po4 = new PurchaseOrder();
        po4.setSupplierId(4);
        po4.setOrderDate(daysAgo(21));
        po4.setStatus("Closed");
        po4.setNotes("Food & beverage stock");
        poRepo.insert(po4);
        insertPOLine(po4.getPoId(), 14, 40, 40);
        insertPOLine(po4.getPoId(), 15, 60, 60);
    }

    private void insertPOLine(int poId, int productId, int ordered, int received) throws SQLException {
        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setPoId(poId);
        line.setProductId(productId);
        line.setQuantityOrdered(ordered);
        line.setQuantityReceived(received);
        poRepo.insertLine(line);
    }

    private void seedCustomerOrders() throws SQLException {
        // Order-1: Acme Corp - InProgress
        Order o1 = new Order();
        o1.setCustomerId(1);
        o1.setOrderDate(daysAgo(2));
        o1.setShipByDate(daysFromNow(5));
        o1.setStatus("Picking");
        o1.setNotes("Rush - handle with care");
        orderRepo.insert(o1);
        insertOrderLine(o1.getOrderId(), 1, 20, 0, 0);
        insertOrderLine(o1.getOrderId(), 3, 30, 0, 0);

        // Order-2: Globex - Pending
        Order o2 = new Order();
        o2.setCustomerId(2);
        o2.setOrderDate(daysAgo(1));
        o2.setShipByDate(daysFromNow(10));
        o2.setStatus("Pending");
        o2.setNotes("Standard delivery");
        orderRepo.insert(o2);
        insertOrderLine(o2.getOrderId(), 4, 5, 0, 0);
        insertOrderLine(o2.getOrderId(), 5, 10, 0, 0);
        insertOrderLine(o2.getOrderId(), 9, 8, 0, 0);

        // Order-3: Initech - Shipped
        Order o3 = new Order();
        o3.setCustomerId(3);
        o3.setOrderDate(daysAgo(10));
        o3.setShipByDate(daysAgo(2));
        o3.setStatus("Shipped");
        o3.setNotes("Delivered on time");
        orderRepo.insert(o3);
        insertOrderLine(o3.getOrderId(), 6, 50, 50, 50);
        insertOrderLine(o3.getOrderId(), 7, 30, 30, 30);
        insertOrderLine(o3.getOrderId(), 8, 100, 100, 100);

        // Order-4: Oceanic - Pending
        Order o4 = new Order();
        o4.setCustomerId(4);
        o4.setOrderDate(daysAgo(0));
        o4.setShipByDate(daysFromNow(14));
        o4.setStatus("Pending");
        o4.setNotes("");
        orderRepo.insert(o4);
        insertOrderLine(o4.getOrderId(), 11, 25, 0, 0);
        insertOrderLine(o4.getOrderId(), 12, 50, 0, 0);
        insertOrderLine(o4.getOrderId(), 13, 15, 0, 0);
        insertOrderLine(o4.getOrderId(), 14, 10, 0, 0);

        // Order-5: Stark Enterprises - Released
        Order o5 = new Order();
        o5.setCustomerId(5);
        o5.setOrderDate(daysAgo(4));
        o5.setShipByDate(daysFromNow(7));
        o5.setStatus("Released");
        o5.setNotes("Fragile items - extra packaging");
        orderRepo.insert(o5);
        insertOrderLine(o5.getOrderId(), 2, 10, 0, 0);
        insertOrderLine(o5.getOrderId(), 10, 20, 0, 0);
    }

    private void insertOrderLine(int orderId, int productId, int ordered, int picked, int shipped) throws SQLException {
        OrderLine line = new OrderLine();
        line.setOrderId(orderId);
        line.setProductId(productId);
        line.setQuantityOrdered(ordered);
        line.setQuantityPicked(picked);
        line.setQuantityShipped(shipped);
        orderRepo.insertLine(line);
    }

    private Date daysFromNow(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, days);
        return c.getTime();
    }

    private Date daysAgo(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -days);
        return c.getTime();
    }
}
