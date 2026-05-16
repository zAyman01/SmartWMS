# SmartWMS Enterprise: Administrator & User Manual

**Version:** 1.0  
**Document Classification:** Internal / Confidential  

---

## 1. Introduction & Overview
SmartWMS is a comprehensive, desktop-based Warehouse Management System engineered to provide total visibility and control over supply chain operations. From inbound procurement and dock receiving to outbound fulfillment and real-time inventory tracking, SmartWMS ensures optimal facility performance.

The system features a modern, dark-themed graphical user interface built on Java Swing and FlatLaf, integrating seamlessly with a dual-database architecture for enterprise reliability.

---

## 2. System Architecture & Prerequisites

### 2.1 Dual-Database Engine
SmartWMS is designed for maximum uptime and zero-configuration local deployments through an intelligent fallback mechanism:
1. **Primary Enterprise Mode (SQL Server):** The system connects to a centralized Microsoft SQL Server database for multi-user synchronization.
2. **Local Fallback Mode (Embedded H2):** If SQL Server is unreachable or not configured, SmartWMS automatically fails over to an embedded, serverless **H2 Database** (`wms-local.mv.db`), ensuring operations can continue without disruption.

### 2.2 Prerequisites
*   **Runtime:** Java Runtime Environment (JRE) 17 or higher.
*   **Resolution:** Minimum display resolution of 1024x768 (1920x1080 recommended).

---

## 3. Getting Started

### 3.1 Initial Setup
Upon launching the application for the very first time on a fresh database, the system will automatically run schema initializations to generate the required tables. The first user to register via the `Register` page will automatically be assigned **Admin** privileges.

### 3.2 Navigation & Interface
*   **The Sidebar:** Located on the left, the sidebar provides instant access to all core warehouse modules.
*   **Contextual Help System ('i' Icon):** In the top-right corner of the menu bar on every page, you will find an **'i' (Info) icon**. Clicking this triggers a business-context popup explaining the exact purpose and operation of the active module.
*   **Barcode Scanner Integration:** Several modules (Receiving, Fulfillment, Inventory) feature a top-mounted scanner listener. When focusing on the application window, USB/Bluetooth barcode scanner inputs are automatically intercepted and processed without needing to click into a specific text field.

---

## 4. Master Data Management

Before you can perform any physical movements, the foundational data must be established.

### 4.1 Products (Catalog)
*   **Purpose:** Acts as the master definition repository for every SKU that enters the facility.
*   **Usage:** Define SKU codes, item names, pricing, and critical physical dimensions (Volume and Weight).
*   **Note:** Dimensions are critical downstream for determining if items can safely fit into specific Bin locations.

### 4.2 Suppliers
*   **Purpose:** The external vendor registry. 
*   **Usage:** Define supplier names, contact details, and payment terms. 
*   **Requirement:** An active supplier must exist before a Purchase Order can be generated.

### 4.3 Customers
*   **Purpose:** The destination endpoint registry for outbound goods.
*   **Usage:** Manage customer names and shipping addresses.
*   **Requirement:** An active customer profile is required to generate a Sales Order.

### 4.4 Bins (Location Management)
*   **Purpose:** The digital map of your physical facility.
*   **Usage:** Define specific shelf locations (e.g., `A1-S2`), categorize them into Zones (e.g., `Bulk`, `Cold Storage`), and set maximum weight/volume constraints.
*   **Requirement:** Goods cannot be received into the warehouse without an active Bin location.

---

## 5. Inbound Logistics

### 5.1 Purchase Orders
*   **Purpose:** The procurement contract. It dictates what goods the warehouse expects to receive.
*   **Process:** 
    1. Click **+ Create PO** and assign a Supplier.
    2. Select the PO from the grid and click **Edit**.
    3. Use the **Manage Lines** interface to add expected SKUs and quantities.

### 5.2 Receiving (Dock Operations)
*   **Purpose:** The gatekeeper module where expected inbound shipments are converted into physical, on-hand inventory.
*   **Process:**
    1. Enter the ID of the incoming Purchase Order and click **Fetch PO**.
    2. The system will populate the grid with expected items.
    3. Use a barcode scanner to verify SKUs, or manually edit the `To Receive (Input)` column.
    4. Click **Receive Items** and assign the goods to a target Bin. The ledger is immediately updated.

---

## 6. Outbound Logistics

### 6.1 Customer Orders
*   **Purpose:** The demand generator. It dictates what needs to leave the warehouse.
*   **Process:** 
    1. Click **+ Create Order**, assign a Customer, and set a priority level.
    2. Use **Manage Lines** to add the requested SKUs.

### 6.2 Fulfillment & Picking
*   **Purpose:** The physical process of locating goods on the shelf and pulling them for packing.
*   **Process:**
    1. **Create Pick Run:** Enter the Order ID and a target Bin to generate a picking manifest.
    2. **Execute:** Load the Pick Run ID. Walk the warehouse floor, scan the items as you pull them from the bins, and verify the quantities in the `Picked (Input)` column.
    3. **Complete:** Finalize the run to deduct the stock from the Bin and transition the Order status toward completion.

---

## 7. Inventory Control & Intelligence

### 7.1 Inventory Management
*   **Purpose:** The absolute truth of current warehouse stock levels and locations.
*   **Capabilities:**
    *   **Search/Scan:** Instantly locate exactly which Bin holds a specific SKU.
    *   **Adjust Stock:** Used during physical cycle counts to correct discrepancies (e.g., accounting for damaged goods). 
    *   **Transfer:** Digitally move stock from one Bin to another to optimize facility space.

### 7.2 Analytics
*   **Purpose:** Real-time visual intelligence for management.
*   **Metrics:** Provides bar charts (Inventory Distribution by Product) and pie charts (Order Health & Status Breakdown) to quickly identify bottlenecks and stockouts.

---

## 8. System Administration

*(Access restricted to users with the `Admin` role)*

### 8.1 User Management
*   **Purpose:** Controls system security and access.
*   **Capabilities:** Provision new employee accounts, reset passwords, and assign roles. When an employee departs, their account should be **Deactivated** rather than deleted to preserve historical audit trails.

### 8.2 Database Backup
*   **Purpose:** Disaster recovery protocol.
*   **Usage:** Select a secure, external directory and click **Backup**. The system will generate a compressed, heavily secured snapshot of the entire relational database to prevent catastrophic data loss in the event of hardware failure.

---

## 9. Compilation & Deployment

For system integrators or IT staff, the software is packaged via Maven.

**To compile the production build:**
```powershell
mvn clean package -DskipTests
```

**To run the production build:**
```powershell
java -jar target/smartwms-1.0-SNAPSHOT.jar
```
