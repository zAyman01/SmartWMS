# SmartWMS

A **Warehouse Management System** (WMS) desktop application built with **Java 17** and **Swing**.  
SmartWMS provides a modern, dark-themed UI for managing every aspect of warehouse operations—from master data and inventory to order fulfillment, receiving, and reporting—with dual-database support (SQL Server + H2 offline fallback).

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
- [Troubleshooting](#troubleshooting)
- [License](#license)

---

## Features

| Module               | Capabilities                                                       |
|----------------------|--------------------------------------------------------------------|
| **Authentication**   | Login, registration, password reset, role-based access (Admin, Supervisor, Picker, Operator) |
| **User Management**  | Full CRUD for users, admin-only access                              |
| **Products**         | SKU-based product catalog with weight, volume, and active status    |
| **Suppliers**        | Supplier directory with contact information                        |
| **Customers**        | Customer directory with contact information                        |
| **Bins / Locations** | Hierarchical warehouse layout (Zone → Aisle → Rack → Shelf → Location) |
| **Purchase Orders**  | Create and manage POs with line items                              |
| **Receiving**        | Receive against POs, auto-update inventory and PO status           |
| **Customer Orders**  | Create and manage outbound orders with line items                  |
| **Fulfillment**      | Pick run creation and completion, inventory deduction               |
| **Inventory**        | Stock adjustments, bin-to-bin transfers, full audit trail           |
| **Analytics**        | KPI dashboard with JFreeChart (stock by product, orders by status) |
| **Reports**          | Printable inventory report via JasperReports                       |
| **Backup**           | H2 database backup to zip file (admin-only)                        |
| **Security**         | SHA-256 password hashing, AES-encrypted "Remember Me" credentials  |

---

## Architecture

```
┌──────────────────────────────────────────────────────┐
│                     UI Layer (Swing)                 │
│  LoginFrame · DashboardFrame · Management Frames     │
│  ThemeConfig (FlatLaf Dark)                          │
├──────────────────────────────────────────────────────┤
│                   Service Layer                      │
│  AuthService · LoginService · ProductService · ...   │
│  InventoryService · FulfillmentService · ...         │
├──────────────────────────────────────────────────────┤
│                  Repository Layer                    │
│  UserRepository · ProductRepository · ...            │
│  (JDBC PreparedStatement, one-connection-per-call)   │
├──────────────────────────────────────────────────────┤
│                    Model Layer                       │
│  User · Product · Supplier · Customer · Bin · ...    │
├──────────────────────────────────────────────────────┤
│                  Config / Infra                      │
│  DatabaseManager · ConnectionPool (HikariCP)         │
│  H2SchemaInitializer · DbConfig                      │
└──────────────────────────────────────────────────────┘
```

**Design Principles:**
- **Layered architecture** — UI → Service → Repository → Database
- **DataSource injection** — services receive a `DataSource` via constructor
- **Dual-database** — SQL Server for production, H2 for offline/development
- **FlatLaf dark theme** — consistent premium look via `ThemeConfig`

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
| JFreeChart        | Dashboard charts                    |
| JasperReports     | Printable inventory reports         |
| JUnit 5           | Automated tests                     |
| SLF4J             | Logging                             |

---

## Project Structure

```
SmartWMS/
├── src/
│   ├── main/
│   │   ├── java/com/warehousewms/
│   │   │   ├── App.java                    # Application entry point
│   │   │   ├── DbConnectionCheck.java      # DB smoke-test runner
│   │   │   ├── LoginMain.java              # CLI login tester
│   │   │   ├── config/                     # Database config, pooling, schema init
│   │   │   ├── model/                      # POJOs (User, Product, Order, ...)
│   │   │   ├── repository/                 # JDBC data access (CRUD)
│   │   │   ├── service/                    # Business logic layer
│   │   │   ├── ui/                         # Swing frames + ThemeConfig
│   │   │   └── util/                       # Session, encryption, credential storage
│   │   └── resources/
│   │       ├── db.properties               # Database connection settings
│   │       ├── db/SmartWMS.sql             # SQL Server schema script
│   │       └── reports/InventoryReport.jrxml
│   └── test/
│       ├── java/com/warehousewms/          # JUnit 5 tests
│       └── resources/db.properties         # Test-specific DB config (in-memory H2)
├── pom.xml
├── phases.md                               # Development phase checklist
└── README.md
```

---

## Getting Started

### Prerequisites

- **JDK 17+** installed and `JAVA_HOME` set
- **Maven 3.8+** (or use the included Maven wrapper)
- *(Optional)* SQL Server 2017+ for production mode

### Build

```bash
# Compile only (skip tests)
mvn -q -DskipTests compile

# Package as fat JAR
mvn -q -DskipTests package
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
mvn -q -DskipTests package

# Run (defaults to H2)
java -jar target/smartwms-1.0-SNAPSHOT.jar

# Run with SQL Server
java -Dwms.useSqlServer=true -jar target/smartwms-1.0-SNAPSHOT.jar
```

### Default Credentials

| Username | Password   | Role  |
|----------|------------|-------|
| `admin`  | `password` | Admin |

### Database Smoke Test

```bash
# Quick connectivity check
mvn -q exec:java -Dexec.mainClass=com.warehousewms.DbConnectionCheck
```

---

## Testing

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=DatabaseManagerTest
```

Tests use an **in-memory H2 database** (configured in `src/test/resources/db.properties`) so they run without any external dependencies.

---

## Database Schema

The system manages 15 tables across the following domains:

| Table              | Purpose                                |
|--------------------|----------------------------------------|
| `Users`            | System users with roles                |
| `Suppliers`        | Supplier directory                     |
| `Products`         | Product catalog (SKU, weight, volume)  |
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
- **`ConnectionPool`** — Manages HikariCP pools for SQL Server and H2
- **`DatabaseManager`** — Selects the active data source with fallback logic
- **`H2SchemaInitializer`** — Creates all H2 tables, indexes, and seeds the admin user

### `model/` — Domain Entities
Plain Java objects mapping to database tables. No framework annotations.

### `repository/` — Data Access
Each repository takes a `DataSource` and provides CRUD methods using `PreparedStatement`. All queries use parameterized SQL to prevent injection.

### `service/` — Business Logic
Services coordinate between repositories and enforce business rules:
- **`ReceivingService`** — Receives against a PO, creates receipt records, updates inventory
- **`FulfillmentService`** — Creates pick runs, completes picks, deducts inventory
- **`InventoryService`** — Stock adjustments and transfers with audit logging

### `ui/` — User Interface
All frames use IntelliJ GUI Designer (`.form` files) with `ThemeConfig` providing a consistent dark theme:
- Color palette, fonts, styled buttons (primary, danger, ghost)
- Styled tables with alternating rows and custom headers
- Search fields with placeholder text

### `util/` — Utilities
- **`SessionContext`** — Thread-safe holder for the currently logged-in user
- **`CredentialStorage`** — AES-encrypted "Remember Me" via Java Preferences API
- **`EncryptionUtil`** — AES encrypt/decrypt for stored credentials

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| SQL Server connection fails | Verify host, port, credentials in `db.properties`. Ensure the database exists. |
| H2 file lock error | Set `AUTO_SERVER=TRUE` in the H2 URL, or close other connections. |
| Schema not initialized | Run `DbConnectionCheck` to verify. Check logs for H2 initialization errors. |
| Login fails with `admin/password` | Ensure H2 schema was initialized (check for `wms-local.mv.db` file). |
| JasperReports error | Ensure the `InventoryReport.jrxml` file exists in `src/main/resources/reports/`. |

---

## License

Internal project — no public license defined.
