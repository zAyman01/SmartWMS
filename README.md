# SmartWMS

A **Warehouse Management System** (WMS) desktop application built with **Java 17** and **Swing**.  
SmartWMS provides a modern, dark-themed UI for managing every aspect of warehouse operations — from master data and inventory to order fulfillment, receiving, analytics, and reporting — with dual-database support (SQL Server + H2 offline fallback) and barcode scanning.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Database Schema](#database-schema)
- [Module Guide](#module-guide)
- [Barcode Scanning](#barcode-scanning)
- [Security](#security)
- [Troubleshooting](#troubleshooting)
- [License](#license)

---

## Features

| Module               | Capabilities                                                       |
|----------------------|--------------------------------------------------------------------|
| **Authentication**   | Login, registration, password reset, role-based access (Admin, Supervisor, Picker, Operator) |
| **User Management**  | Full CRUD for users, admin-only access                              |
| **Products**         | SKU-based product catalog with weight, volume, barcode, active status |
| **Suppliers**        | Supplier directory with contact information                        |
| **Customers**        | Customer directory with contact information                        |
| **Bins / Locations** | Hierarchical warehouse layout (Zone -> Aisle -> Rack -> Shelf -> Location) |
| **Purchase Orders**  | Create and manage POs with line items                              |
| **Receiving**        | Receive against POs, auto-update inventory and PO status           |
| **Customer Orders**  | Create and manage outbound orders with line items                  |
| **Fulfillment**      | Pick run creation and completion, inventory deduction               |
| **Inventory**        | Stock adjustments, bin-to-bin transfers, full audit trail           |
| **Barcode Scanning** | Keyboard-wedge USB scanner + optional camera (ZXing) support       |
| **Analytics**        | KPI dashboard with JFreeChart (stock by product, orders by status) |
| **Reports**          | Printable inventory report via JasperReports                       |
| **Backup**           | H2 database backup to zip file (admin-only)                        |
| **Security**         | PBKDF2 password hashing, AES-256-GCM encrypted "Remember Me", audit log |

---

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                     UI Layer (Swing)                         │
│  LoginFrame · DashboardFrame · Management Frames             │
│  BarcodeScannerPanel · CameraScannerDialog                   │
│  ThemeConfig (FlatLaf Dark)                                  │
├──────────────────────────────────────────────────────────────┤
│                     Service Layer                            │
│  AuthService · LoginService · ProductService · ...           │
│  InventoryService · FulfillmentService · ReceivingService    │
├──────────────────────────────────────────────────────────────┤
│                    Repository Layer                          │
│  UserRepository · ProductRepository · ...                    │
│  (JDBC PreparedStatement, parameterized queries)             │
├──────────────────────────────────────────────────────────────┤
│                     Model Layer                              │
│  User · Product · Supplier · Customer · Bin · Order · ...    │
├──────────────────────────────────────────────────────────────┤
│                   Config / Infra                             │
│  DatabaseManager · ConnectionPool (HikariCP)                 │
│  H2SchemaInitializer · DbConfig                              │
│  CameraSupport (platform-aware camera probe)                 │
└──────────────────────────────────────────────────────────────┘
```

**Design Principles:**
- **Layered architecture** — UI -> Service -> Repository -> Database
- **DataSource injection** — services receive a `DataSource` via constructor
- **Dual-database** — SQL Server for production, H2 for offline/development
- **FlatLaf dark theme** — consistent premium look via `ThemeConfig`
- **Transactional integrity** — inventory transfers, pick runs, and order fulfillment use manual JDBC transactions with rollback
- **Platform-adaptive camera** — gracefully handles unsupported platforms (ARM/Raspberry Pi) without crashing

---

## Tech Stack

| Technology        | Purpose                             |
|-------------------|-------------------------------------|
| Java 17           | Language and runtime                |
| Maven             | Build and dependency management     |
| Swing + FlatLaf   | Desktop UI with modern dark theme   |
| HikariCP          | JDBC connection pooling             |
| SQL Server (MSSQL)| Production database                 |
| H2                | Embedded offline fallback database  |
| ZXing 3.5.3       | Barcode decoding (camera)           |
| webcam-capture    | Webcam access                       |
| JFreeChart        | Dashboard charts                    |
| JasperReports     | Printable inventory reports         |
| JUnit 5           | Automated tests                     |
| SLF4J + Logback   | Logging                             |

---

## Project Structure

```
SmartWMS/
+-- src/
|   +-- main/
|   |   +-- java/com/warehousewms/
|   |   |   +-- App.java                    # Application entry point
|   |   |   +-- SeedData.java               # Sample data seeder
|   |   |   +-- DbConnectionCheck.java      # DB smoke-test runner
|   |   |   +-- config/                     # Database config, pooling, schema init
|   |   |   |   +-- AppConfig.java
|   |   |   |   +-- ConnectionPool.java
|   |   |   |   +-- DatabaseManager.java
|   |   |   |   +-- DbConfig.java
|   |   |   |   +-- H2SchemaInitializer.java
|   |   |   +-- model/                      # POJOs (User, Product, Order, ...)
|   |   |   +-- repository/                 # JDBC data access (CRUD)
|   |   |   +-- service/                    # Business logic layer
|   |   |   |   +-- AuthService.java
|   |   |   |   +-- FulfillmentService.java
|   |   |   |   +-- InventoryService.java
|   |   |   |   +-- LoginService.java
|   |   |   |   +-- OrderService.java
|   |   |   |   +-- ProductService.java
|   |   |   |   +-- PurchaseOrderService.java
|   |   |   |   +-- ReceivingService.java
|   |   |   |   +-- ReportService.java
|   |   |   |   +-- DatabaseAdminService.java
|   |   |   +-- ui/                         # Swing frames + ThemeConfig
|   |   |   |   +-- ThemeConfig.java
|   |   |   |   +-- LoginFrame.java
|   |   |   |   +-- DashboardFrame.java
|   |   |   |   +-- BarcodeScannerPanel.java  # Reusable scanner widget
|   |   |   |   +-- CameraScannerDialog.java  # Camera-based barcode capture
|   |   |   |   +-- *.java (15+ frame files)
|   |   |   +-- util/                       # Session, encryption, camera probe
|   |   |       +-- SessionContext.java
|   |   |       +-- EncryptionUtil.java
|   |   |       +-- CredentialStorage.java
|   |   |       +-- CameraSupport.java
|   |   +-- resources/
|   |       +-- db.properties               # Database connection settings
|   |       +-- db/SmartWMS.sql             # SQL Server schema script
|   |       +-- reports/InventoryReport.jrxml
|   +-- test/
|       +-- java/com/warehousewms/          # JUnit 5 tests
|       +-- resources/db.properties
+-- pom.xml
+-- README.md
```

---

## Getting Started

### Prerequisites

- **JDK 17+** installed and `JAVA_HOME` set
- **Maven 3.8+**
- _(Optional)_ SQL Server 2017+ for production mode

### Build

```bash
# Compile (skip tests)
mvn -DskipTests compile

# Package as fat JAR
mvn -DskipTests package

# Full build with tests
mvn clean package
```

---

## Configuration

### Database Properties

Edit `src/main/resources/db.properties`:

```properties
# SQL Server
db.sqlserver.host=localhost
db.sqlserver.port=1433
db.sqlserver.database=SmartWMS
db.sqlserver.user=sa
db.sqlserver.password=YOUR_PASSWORD

# H2 local fallback
db.h2.url=jdbc:h2:file:./wms-local;AUTO_SERVER=TRUE
```

### Database Selection Logic

1. If `-Dwms.useSqlServer=true` is set, the app tries SQL Server first.
2. If SQL Server is unreachable, it **automatically falls back to H2**.
3. Without the flag, H2 is used directly.
4. On H2 startup, all tables, indexes, and a default admin user are created automatically.

### SQL Server Setup

Run the schema script on your SQL Server instance:
```bash
sqlcmd -S localhost -i src/main/resources/db/SmartWMS.sql
```

---

## Running the Application

### From IDE

Run `com.warehousewms.App` — launches the login screen.

### From Command Line

```bash
# Build fat JAR
mvn -DskipTests package

# Run (defaults to H2)
java -jar target/smartwms-1.0-SNAPSHOT.jar

# Run with SQL Server
java -Dwms.useSqlServer=true -jar target/smartwms-1.0-SNAPSHOT.jar
```

### Default Credentials

| Username | Password   | Role  |
|----------|------------|-------|
| `admin`  | `admin123` | Admin |

Additional seed users (`alice`, `bob`, `carol`, `dave`) are created with password `username` + `123` (e.g., `alice123`).

### Database Smoke Test

```bash
mvn exec:java -Dexec.mainClass=com.warehousewms.DbConnectionCheck
```

---

## Testing

```bash
# Run all 65+ tests
mvn clean test

# Run a specific test class
mvn test -Dtest=BinRepositoryTest
```

All tests use an **in-memory H2 database** and require no external dependencies. Each test class initializes a fresh schema via `@BeforeEach`.

**Test coverage includes:**
- Repository CRUD operations for all entities
- Password hashing (PBKDF2) — salt uniqueness, verification, rejections
- Encryption round-trip (AES-256-GCM) — encrypt/decrypt, tamper detection
- Login service — valid auth, wrong password, empty input, non-existent user
- Inventory service — stock adjustments (create, update, floor at zero), transfers (success, insufficient stock, exceeds available)
- Order/PO service — creation with line items in a transaction
- Database connection — H2 initialization, SQL Server fallback
- Session context — thread safety, role checking, clear

---

## Database Schema

The system manages 15 tables across the following domains:

| Table              | Purpose                                |
|--------------------|----------------------------------------|
| `Users`            | System users with roles                |
| `Suppliers`        | Supplier directory                     |
| `Products`         | Product catalog (SKU, weight, volume, barcode) |
| `Bins`             | Hierarchical warehouse locations       |
| `Inventory`        | Stock per product per bin              |
| `Customers`        | Customer directory                     |
| `PurchaseOrders`   | Inbound purchase orders                |
| `PurchaseOrderLines` | PO line items                        |
| `Receipts`         | Goods-received records                 |
| `ReceiptLines`     | Receipt line items                     |
| `Orders`           | Outbound customer orders               |
| `OrderLines`       | Order line items                       |
| `PickRuns`         | Pick run headers                       |
| `PickRunItems`     | Individual pick tasks                  |
| `AuditLog`         | Change tracking for all entities       |

---

## Module Guide

### `config/` — Infrastructure
- **`AppConfig`** — Application name and version constants
- **`DbConfig`** — Loads `db.properties`, provides JDBC URLs and credentials
- **`ConnectionPool`** — Manages HikariCP pools for SQL Server and H2 (double-checked locking)
- **`DatabaseManager`** — Selects the active data source with fallback logic
- **`H2SchemaInitializer`** — Creates all H2 tables, indexes, and seeds the admin user

### `model/` — Domain Entities
Plain Java objects mapping to database tables. No framework annotations.

### `repository/` — Data Access
Each repository takes a `DataSource` and provides CRUD methods using `PreparedStatement`. All queries use parameterized SQL to prevent injection. Several repositories provide `Connection`-overloaded methods for transactional callers. Generated keys are retrieved via `RETURN_GENERATED_KEYS`.

### `service/` — Business Logic
Services coordinate between repositories and enforce business rules:

| Service                | Key Responsibilities                        | Transactional |
|------------------------|---------------------------------------------|:---:|
| `LoginService`         | Authentication, session setup, "Remember Me"| No  |
| `AuthService`          | User CRUD, password reset                   | No  |
| `InventoryService`     | Stock adjustments, bin-to-bin transfers     | Yes |
| `ReceivingService`     | Receive against PO, update inventory        | Yes |
| `FulfillmentService`   | Pick run creation and completion            | Yes |
| `OrderService`         | Customer order creation                     | Yes |
| `PurchaseOrderService` | PO creation                                 | Yes |
| `ReportService`        | JasperReports generation                    | No  |
| `DatabaseAdminService` | H2 backup to zip                            | No  |

### `ui/` — User Interface
All frames are built with Swing and follow a consistent dark theme via `ThemeConfig`:

- **`ThemeConfig`** — Centralized palette, fonts, styled button factories (`primaryButton`, `dangerButton`, `ghostButton`), table styling, search fields, emoji font resolution
- **`LoginFrame`** — Login with "Remember Me" and "Forgot Password" flows
- **`DashboardFrame`** — Sidebar navigation with role-based visibility, stat cards, quick actions
- **`BarcodeScannerPanel`** — Reusable panel with text field input and optional camera button
- **`CameraScannerDialog`** — Modal dialog with live webcam feed and ZXing barcode decoding

### `util/` — Utilities
- **`SessionContext`** — Thread-safe holder for the currently logged-in user
- **`EncryptionUtil`** — AES-256-GCM encrypt/decrypt for stored credentials
- **`CredentialStorage`** — Encrypted "Remember Me" via Java Preferences API
- **`CameraSupport`** — Runtime probe for webcam-capture availability (handles unsupported platforms)

---

## Barcode Scanning

SmartWMS supports two methods of barcode scanning:

### 1. Keyboard-Wedge Scanner (all platforms)
USB barcode scanners that emulate keyboard input work out of the box — no configuration needed. Scan results are captured by the `BarcodeScannerPanel` text field on all three operational frames (Receiving, Fulfillment, Inventory).

### 2. Camera Scanning (ZXing + webcam-capture)
The camera button opens `CameraScannerDialog`, which uses the device webcam with ZXing for real-time barcode decoding.

**Platform support:**
- **Windows / macOS / Linux x64** — fully supported via BridJ native libraries
- **ARM / Raspberry Pi** — gracefully detected by `CameraSupport`; the camera button is hidden and a warning is shown. Keyboard-wedge scanners remain fully operational.
- **No webcam** — detected at runtime; camera button hidden, no crash.

**Integration with workflows:**
- **Receiving** — scanning a barcode auto-fills the matching PO line with the remaining quantity to receive
- **Fulfillment** — scanning a barcode auto-fills the matching order line's picked quantity
- **Inventory** — scanning a barcode filters the inventory table to that product

**Barcode data model:**
- Products have a separate `Barcode` column (distinct from `SKU`)
- `findByBarcode()` searches both `Barcode` and `SKU` columns as a fallback
- Seed data includes unique EAN-13 barcodes for all 15 products

---

## Security

| Feature              | Implementation                                    |
|----------------------|---------------------------------------------------|
| Password storage     | PBKDF2WithHmacSHA256 with random 16-byte salt     |
| Credential storage   | AES-256-GCM with PBKDF2-derived key, stored in `java.util.prefs` |
| SQL injection        | Parameterized queries via `PreparedStatement` throughout |
| Backup injection     | Input sanitisation before embedding in SQL         |
| Audit trail          | All inventory changes logged to `AuditLog` table  |
| Role-based access    | Admin-only sections hidden in navigation           |

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| SQL Server connection fails | Verify host, port, credentials in `db.properties`. Ensure the database exists and TCP/IP is enabled. |
| H2 file lock error | Set `AUTO_SERVER=TRUE` in the H2 URL, or close other connections. |
| Schema not initialized | Run `DbConnectionCheck` to verify. Check logs for H2 initialization errors. |
| Login fails with `admin/admin123` | Ensure H2 schema was initialized (check for `wms-local.mv.db` file). Delete it and restart to reinitialize. |
| JasperReports error | Ensure `InventoryReport.jrxml` exists in `src/main/resources/reports/`. |
| Camera button not showing | Webcam-capture library is not compatible with this platform. Use a keyboard-wedge USB scanner instead. |
| Camera fails to open | Ensure no other application is using the webcam. Check OS permissions for camera access. |

---

## License

This project is provided for evaluation and educational purposes. No public license is defined.
