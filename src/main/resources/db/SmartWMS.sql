-- =============================================
-- Smart WMS – Full Database Recreation Script
-- Target: Microsoft SQL Server 2017+
-- =============================================
USE master;
GO

-- Drop existing database if present (CAUTION: deletes all data)
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'SmartWMS')
BEGIN
    ALTER DATABASE SmartWMS SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE SmartWMS;
END
GO

-- Create the database with modern collation
CREATE DATABASE SmartWMS
COLLATE Latin1_General_100_CI_AS_SC_UTF8;
GO

USE SmartWMS;
GO

-- =============================================
-- 1. USERS
-- =============================================
CREATE TABLE Users (
    UserId          INT IDENTITY(1,1)   NOT NULL,
    Username        NVARCHAR(50)        NOT NULL,
    PasswordHash    NVARCHAR(255)       NOT NULL,
    FullName        NVARCHAR(100)       NOT NULL,
    Role            NVARCHAR(20)        NOT NULL,
    CreatedAt       DATETIME2           NOT NULL CONSTRAINT DF_Users_CreatedAt DEFAULT GETDATE(),
    CONSTRAINT PK_Users PRIMARY KEY CLUSTERED (UserId),
    CONSTRAINT UQ_Users_Username UNIQUE (Username),
    CONSTRAINT CK_Users_Role CHECK (Role IN ('Admin','Supervisor','Picker','Operator'))
);
GO

-- =============================================
-- 2. SUPPLIERS
-- =============================================
CREATE TABLE Suppliers (
    SupplierId      INT IDENTITY(1,1)   NOT NULL,
    Name            NVARCHAR(200)       NOT NULL,
    ContactName     NVARCHAR(100)       NULL,
    Email           NVARCHAR(100)       NULL,
    Phone           NVARCHAR(50)        NULL,
    CONSTRAINT PK_Suppliers PRIMARY KEY CLUSTERED (SupplierId)
);
GO

-- =============================================
-- 3. PRODUCTS
-- =============================================
CREATE TABLE Products (
    ProductId       INT IDENTITY(1,1)   NOT NULL,
    SKU             NVARCHAR(50)        NOT NULL,
    Name            NVARCHAR(200)       NOT NULL,
    ImagePath       NVARCHAR(500)       NULL,
    UnitWeightKg    DECIMAL(10,3)       NOT NULL CONSTRAINT DF_Products_UnitWeightKg DEFAULT 0,
    UnitVolumeM3    DECIMAL(10,6)       NOT NULL CONSTRAINT DF_Products_UnitVolumeM3 DEFAULT 0,
    IsActive        BIT                 NOT NULL CONSTRAINT DF_Products_IsActive DEFAULT 1,
    CONSTRAINT PK_Products PRIMARY KEY CLUSTERED (ProductId),
    CONSTRAINT UQ_Products_SKU UNIQUE (SKU)
);
GO

-- =============================================
-- 4. BINS (hierarchical warehouse locations)
-- =============================================
CREATE TABLE Bins (
    BinId           INT IDENTITY(1,1)   NOT NULL,
    ParentBinId     INT                 NULL,
    Name            NVARCHAR(50)        NOT NULL,
    BinType         NVARCHAR(20)        NOT NULL,
    MaxWeightKg     DECIMAL(10,3)       NULL,
    MaxVolumeM3     DECIMAL(10,6)       NULL,
    SortOrder       INT                 NOT NULL CONSTRAINT DF_Bins_SortOrder DEFAULT 0,
    CONSTRAINT PK_Bins PRIMARY KEY CLUSTERED (BinId),
    CONSTRAINT FK_Bins_ParentBin FOREIGN KEY (ParentBinId) REFERENCES Bins(BinId),
    CONSTRAINT CK_Bins_BinType CHECK (BinType IN ('Zone','Aisle','Rack','Shelf','Location'))
);
GO

-- =============================================
-- 5. INVENTORY (stock per product per bin)
-- =============================================
CREATE TABLE Inventory (
    InventoryId     INT IDENTITY(1,1)   NOT NULL,
    ProductId       INT                 NOT NULL,
    BinId           INT                 NOT NULL,
    Quantity        INT                 NOT NULL CONSTRAINT DF_Inventory_Quantity DEFAULT 0,
    LotNumber       NVARCHAR(50)        NULL,
    ExpiryDate      DATE                NULL,
    CONSTRAINT PK_Inventory PRIMARY KEY CLUSTERED (InventoryId),
    CONSTRAINT FK_Inventory_Product FOREIGN KEY (ProductId) REFERENCES Products(ProductId),
    CONSTRAINT FK_Inventory_Bin FOREIGN KEY (BinId) REFERENCES Bins(BinId),
    CONSTRAINT UQ_Inventory_Product_Bin UNIQUE (ProductId, BinId, LotNumber, ExpiryDate)
);
GO

-- =============================================
-- 6. CUSTOMERS
-- =============================================
CREATE TABLE Customers (
    CustomerId      INT IDENTITY(1,1)   NOT NULL,
    Name            NVARCHAR(200)       NOT NULL,
    ContactName     NVARCHAR(100)       NULL,
    Email           NVARCHAR(100)       NULL,
    Phone           NVARCHAR(50)        NULL,
    CONSTRAINT PK_Customers PRIMARY KEY CLUSTERED (CustomerId)
);
GO

-- =============================================
-- 7. PURCHASE ORDERS
-- =============================================
CREATE TABLE PurchaseOrders (
    POId            INT IDENTITY(1,1)   NOT NULL,
    SupplierId      INT                 NOT NULL,
    OrderDate       DATETIME2           NOT NULL CONSTRAINT DF_PurchaseOrders_OrderDate DEFAULT GETDATE(),
    Status          NVARCHAR(20)        NOT NULL CONSTRAINT DF_PurchaseOrders_Status DEFAULT 'Open',
    Notes           NVARCHAR(MAX)       NULL,
    CONSTRAINT PK_PurchaseOrders PRIMARY KEY CLUSTERED (POId),
    CONSTRAINT FK_PurchaseOrders_Supplier FOREIGN KEY (SupplierId) REFERENCES Suppliers(SupplierId),
    CONSTRAINT CK_PurchaseOrders_Status CHECK (Status IN ('Open','Closed','Cancelled'))
);
GO

-- =============================================
-- 8. PURCHASE ORDER LINES
-- =============================================
CREATE TABLE PurchaseOrderLines (
    POLineId        INT IDENTITY(1,1)   NOT NULL,
    POId            INT                 NOT NULL,
    ProductId       INT                 NOT NULL,
    QuantityOrdered INT                 NOT NULL,
    QuantityReceived INT                NOT NULL CONSTRAINT DF_POLines_QuantityReceived DEFAULT 0,
    CONSTRAINT PK_PurchaseOrderLines PRIMARY KEY CLUSTERED (POLineId),
    CONSTRAINT FK_POLines_PO FOREIGN KEY (POId) REFERENCES PurchaseOrders(POId),
    CONSTRAINT FK_POLines_Product FOREIGN KEY (ProductId) REFERENCES Products(ProductId)
);
GO

-- =============================================
-- 9. RECEIPTS (goods received)
-- =============================================
CREATE TABLE Receipts (
    ReceiptId       INT IDENTITY(1,1)   NOT NULL,
    POId            INT                 NULL,
    ReceiptDate     DATETIME2           NOT NULL CONSTRAINT DF_Receipts_ReceiptDate DEFAULT GETDATE(),
    Status          NVARCHAR(20)        NOT NULL CONSTRAINT DF_Receipts_Status DEFAULT 'Completed',
    Notes           NVARCHAR(MAX)       NULL,
    CONSTRAINT PK_Receipts PRIMARY KEY CLUSTERED (ReceiptId),
    CONSTRAINT FK_Receipts_PO FOREIGN KEY (POId) REFERENCES PurchaseOrders(POId),
    CONSTRAINT CK_Receipts_Status CHECK (Status IN ('Completed','Partially Received','Verified'))
);
GO

-- =============================================
-- 10. RECEIPT LINES
-- =============================================
CREATE TABLE ReceiptLines (
    ReceiptLineId   INT IDENTITY(1,1)   NOT NULL,
    ReceiptId       INT                 NOT NULL,
    ProductId       INT                 NOT NULL,
    BinId           INT                 NOT NULL,
    Quantity        INT                 NOT NULL,
    LotNumber       NVARCHAR(50)        NULL,
    ExpiryDate      DATE                NULL,
    CONSTRAINT PK_ReceiptLines PRIMARY KEY CLUSTERED (ReceiptLineId),
    CONSTRAINT FK_ReceiptLines_Receipt FOREIGN KEY (ReceiptId) REFERENCES Receipts(ReceiptId),
    CONSTRAINT FK_ReceiptLines_Product FOREIGN KEY (ProductId) REFERENCES Products(ProductId),
    CONSTRAINT FK_ReceiptLines_Bin FOREIGN KEY (BinId) REFERENCES Bins(BinId)
);
GO

-- =============================================
-- 11. CUSTOMER ORDERS
-- =============================================
CREATE TABLE Orders (
    OrderId         INT IDENTITY(1,1)   NOT NULL,
    CustomerId      INT                 NOT NULL,
    OrderDate       DATETIME2           NOT NULL CONSTRAINT DF_Orders_OrderDate DEFAULT GETDATE(),
    ShipByDate      DATETIME2           NULL,
    Status          NVARCHAR(20)        NOT NULL CONSTRAINT DF_Orders_Status DEFAULT 'Pending',
    Notes           NVARCHAR(MAX)       NULL,
    CONSTRAINT PK_Orders PRIMARY KEY CLUSTERED (OrderId),
    CONSTRAINT FK_Orders_Customer FOREIGN KEY (CustomerId) REFERENCES Customers(CustomerId),
    CONSTRAINT CK_Orders_Status CHECK (Status IN ('Pending','Released','Picking','Picked','Packed','Shipped','Cancelled'))
);
GO

-- =============================================
-- 12. ORDER LINES
-- =============================================
CREATE TABLE OrderLines (
    OrderLineId     INT IDENTITY(1,1)   NOT NULL,
    OrderId         INT                 NOT NULL,
    ProductId       INT                 NOT NULL,
    QuantityOrdered INT                 NOT NULL,
    QuantityPicked  INT                 NOT NULL CONSTRAINT DF_OrderLines_QuantityPicked DEFAULT 0,
    QuantityShipped INT                 NOT NULL CONSTRAINT DF_OrderLines_QuantityShipped DEFAULT 0,
    CONSTRAINT PK_OrderLines PRIMARY KEY CLUSTERED (OrderLineId),
    CONSTRAINT FK_OrderLines_Order FOREIGN KEY (OrderId) REFERENCES Orders(OrderId),
    CONSTRAINT FK_OrderLines_Product FOREIGN KEY (ProductId) REFERENCES Products(ProductId)
);
GO

-- =============================================
-- 13. PICK RUNS
-- =============================================
CREATE TABLE PickRuns (
    PickRunId       INT IDENTITY(1,1)   NOT NULL,
    AssignedToUserId INT                NULL,
    StartedAt       DATETIME2           NULL,
    CompletedAt     DATETIME2           NULL,
    Status          NVARCHAR(20)        NOT NULL CONSTRAINT DF_PickRuns_Status DEFAULT 'Created',
    CONSTRAINT PK_PickRuns PRIMARY KEY CLUSTERED (PickRunId),
    CONSTRAINT FK_PickRuns_User FOREIGN KEY (AssignedToUserId) REFERENCES Users(UserId),
    CONSTRAINT CK_PickRuns_Status CHECK (Status IN ('Created','InProgress','Completed','Cancelled'))
);
GO

-- =============================================
-- 14. PICK RUN ITEMS
-- =============================================
CREATE TABLE PickRunItems (
    PickRunItemId   INT IDENTITY(1,1)   NOT NULL,
    PickRunId       INT                 NOT NULL,
    OrderLineId     INT                 NOT NULL,
    BinId           INT                 NOT NULL,
    QuantityToPick  INT                 NOT NULL,
    QuantityPicked  INT                 NOT NULL CONSTRAINT DF_PickRunItems_QuantityPicked DEFAULT 0,
    Status          NVARCHAR(20)        NOT NULL CONSTRAINT DF_PickRunItems_Status DEFAULT 'Pending',
    CONSTRAINT PK_PickRunItems PRIMARY KEY CLUSTERED (PickRunItemId),
    CONSTRAINT FK_PickRunItems_PickRun FOREIGN KEY (PickRunId) REFERENCES PickRuns(PickRunId),
    CONSTRAINT FK_PickRunItems_OrderLine FOREIGN KEY (OrderLineId) REFERENCES OrderLines(OrderLineId),
    CONSTRAINT FK_PickRunItems_Bin FOREIGN KEY (BinId) REFERENCES Bins(BinId),
    CONSTRAINT CK_PickRunItems_Status CHECK (Status IN ('Pending','Picked','ShortPicked','Skipped'))
);
GO

-- =============================================
-- 15. AUDIT LOG
-- =============================================
CREATE TABLE AuditLog (
    AuditId         INT IDENTITY(1,1)   NOT NULL,
    TableName       NVARCHAR(128)       NOT NULL,
    RecordId        INT                 NOT NULL,
    ActionType      NVARCHAR(10)        NOT NULL,
    ColumnName      NVARCHAR(128)       NULL,
    OldValue        NVARCHAR(MAX)       NULL,
    NewValue        NVARCHAR(MAX)       NULL,
    ChangedByUserId INT                 NOT NULL,
    ChangedAt       DATETIME2           NOT NULL CONSTRAINT DF_AuditLog_ChangedAt DEFAULT GETDATE(),
    CONSTRAINT PK_AuditLog PRIMARY KEY CLUSTERED (AuditId),
    CONSTRAINT FK_AuditLog_User FOREIGN KEY (ChangedByUserId) REFERENCES Users(UserId),
    CONSTRAINT CK_AuditLog_ActionType CHECK (ActionType IN ('INSERT','UPDATE','DELETE'))
);
GO

-- =============================================
-- PERFORMANCE INDEXES (non‑clustered)
-- =============================================
CREATE NONCLUSTERED INDEX IX_Inventory_ProductId ON Inventory(ProductId);
CREATE NONCLUSTERED INDEX IX_Inventory_BinId ON Inventory(BinId);
CREATE NONCLUSTERED INDEX IX_OrderLines_OrderId ON OrderLines(OrderId);
CREATE NONCLUSTERED INDEX IX_OrderLines_ProductId ON OrderLines(ProductId);
CREATE NONCLUSTERED INDEX IX_PickRunItems_PickRunId ON PickRunItems(PickRunId);
CREATE NONCLUSTERED INDEX IX_AuditLog_Table_Record ON AuditLog(TableName, RecordId);
GO

-- =============================================
-- DEFAULT DATA: Admin user (password = 'password')
-- Password hash is SHA-256 of 'password'
-- =============================================
INSERT INTO Users (Username, PasswordHash, FullName, Role)
VALUES (N'admin', N'5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', N'System Administrator', N'Admin');
GO

PRINT N'Smart WMS database recreated successfully.';
GO