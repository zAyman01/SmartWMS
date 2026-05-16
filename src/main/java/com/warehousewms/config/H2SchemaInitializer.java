package com.warehousewms.config;

import com.warehousewms.repository.UserRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class H2SchemaInitializer {
    public void ensureSchema(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Users ("
                            + "UserId INT AUTO_INCREMENT PRIMARY KEY, "
                            + "Username VARCHAR(50) NOT NULL UNIQUE, "
                            + "PasswordHash VARCHAR(255) NOT NULL, "
                            + "FullName VARCHAR(100) NOT NULL, "
                            + "Role VARCHAR(20) NOT NULL, "
                            + "CreatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                            + "CONSTRAINT CK_Users_Role CHECK (Role IN ('Admin','Supervisor','Picker','Operator'))"
                            + ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Suppliers ("
                            + "SupplierId INT AUTO_INCREMENT PRIMARY KEY, "
                            + "Name VARCHAR(200) NOT NULL, "
                            + "ContactName VARCHAR(100) NULL, "
                            + "Email VARCHAR(100) NULL, "
                            + "Phone VARCHAR(50) NULL"
                            + ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Products ("
                            + "ProductId INT AUTO_INCREMENT PRIMARY KEY, "
                            + "SKU VARCHAR(50) NOT NULL UNIQUE, "
                            + "Name VARCHAR(200) NOT NULL, "
                            + "ImagePath VARCHAR(500) NULL, "
                            + "UnitWeightKg DECIMAL(10,3) NOT NULL DEFAULT 0, "
                            + "UnitVolumeM3 DECIMAL(10,6) NOT NULL DEFAULT 0, "
                            + "IsActive BOOLEAN NOT NULL DEFAULT TRUE, "
                            + "Barcode VARCHAR(100) NULL"
                            + ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Bins ("
                            + "BinId INT AUTO_INCREMENT PRIMARY KEY, "
                            + "ParentBinId INT NULL, "
                            + "Name VARCHAR(50) NOT NULL, "
                            + "BinType VARCHAR(20) NOT NULL, "
                            + "MaxWeightKg DECIMAL(10,3) NULL, "
                            + "MaxVolumeM3 DECIMAL(10,6) NULL, "
                            + "SortOrder INT NOT NULL DEFAULT 0, "
                            + "CONSTRAINT FK_Bins_ParentBin FOREIGN KEY (ParentBinId) REFERENCES Bins(BinId), "
                            + "CONSTRAINT CK_Bins_BinType CHECK (BinType IN ('Zone','Aisle','Rack','Shelf','Location'))"
                            + ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Inventory ("
                            + "InventoryId INT AUTO_INCREMENT PRIMARY KEY, "
                            + "ProductId INT NOT NULL, "
                            + "BinId INT NOT NULL, "
                            + "Quantity INT NOT NULL DEFAULT 0, "
                            + "LotNumber VARCHAR(50) NULL, "
                            + "ExpiryDate DATE NULL, "
                            + "CONSTRAINT FK_Inventory_Product FOREIGN KEY (ProductId) REFERENCES Products(ProductId), "
                            + "CONSTRAINT FK_Inventory_Bin FOREIGN KEY (BinId) REFERENCES Bins(BinId), "
                            + "CONSTRAINT UQ_Inventory_Product_Bin UNIQUE (ProductId, BinId, LotNumber, ExpiryDate)"
                            + ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Customers ("
                            + "CustomerId INT AUTO_INCREMENT PRIMARY KEY, "
                            + "Name VARCHAR(200) NOT NULL, "
                            + "ContactName VARCHAR(100) NULL, "
                            + "Email VARCHAR(100) NULL, "
                            + "Phone VARCHAR(50) NULL"
                            + ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS PurchaseOrders ("
                            + "POId INT AUTO_INCREMENT PRIMARY KEY, "
                            + "SupplierId INT NOT NULL, "
                            + "OrderDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                            + "Status VARCHAR(20) NOT NULL DEFAULT 'Open', "
                            + "Notes CLOB NULL, "
                            + "CONSTRAINT FK_PurchaseOrders_Supplier FOREIGN KEY (SupplierId) REFERENCES Suppliers(SupplierId), "
                            + "CONSTRAINT CK_PurchaseOrders_Status CHECK (Status IN ('Open','Closed','Cancelled'))"
                            + ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS PurchaseOrderLines ("
                            + "POLineId INT AUTO_INCREMENT PRIMARY KEY, "
                            + "POId INT NOT NULL, "
                            + "ProductId INT NOT NULL, "
                            + "QuantityOrdered INT NOT NULL, "
                            + "QuantityReceived INT NOT NULL DEFAULT 0, "
                            + "CONSTRAINT FK_POLines_PO FOREIGN KEY (POId) REFERENCES PurchaseOrders(POId), "
                            + "CONSTRAINT FK_POLines_Product FOREIGN KEY (ProductId) REFERENCES Products(ProductId)"
                            + ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Receipts ("
                            + "ReceiptId INT AUTO_INCREMENT PRIMARY KEY, "
                            + "POId INT NULL, "
                            + "ReceiptDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                            + "Status VARCHAR(20) NOT NULL DEFAULT 'Completed', "
                            + "Notes CLOB NULL, "
                            + "CONSTRAINT FK_Receipts_PO FOREIGN KEY (POId) REFERENCES PurchaseOrders(POId), "
                            + "CONSTRAINT CK_Receipts_Status CHECK (Status IN ('Completed','Partially Received','Verified'))"
                            + ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ReceiptLines ("
                            + "ReceiptLineId INT AUTO_INCREMENT PRIMARY KEY, "
                            + "ReceiptId INT NOT NULL, "
                            + "ProductId INT NOT NULL, "
                            + "BinId INT NOT NULL, "
                            + "Quantity INT NOT NULL, "
                            + "LotNumber VARCHAR(50) NULL, "
                            + "ExpiryDate DATE NULL, "
                            + "CONSTRAINT FK_ReceiptLines_Receipt FOREIGN KEY (ReceiptId) REFERENCES Receipts(ReceiptId), "
                            + "CONSTRAINT FK_ReceiptLines_Product FOREIGN KEY (ProductId) REFERENCES Products(ProductId), "
                            + "CONSTRAINT FK_ReceiptLines_Bin FOREIGN KEY (BinId) REFERENCES Bins(BinId)"
                            + ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Orders ("
                            + "OrderId INT AUTO_INCREMENT PRIMARY KEY, "
                            + "CustomerId INT NOT NULL, "
                            + "OrderDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                            + "ShipByDate TIMESTAMP NULL, "
                            + "Status VARCHAR(20) NOT NULL DEFAULT 'Pending', "
                            + "Notes CLOB NULL, "
                            + "CONSTRAINT FK_Orders_Customer FOREIGN KEY (CustomerId) REFERENCES Customers(CustomerId), "
                            + "CONSTRAINT CK_Orders_Status CHECK (Status IN ('Pending','Released','Picking','Picked','Packed','Shipped','Cancelled'))"
                            + ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS OrderLines ("
                            + "OrderLineId INT AUTO_INCREMENT PRIMARY KEY, "
                            + "OrderId INT NOT NULL, "
                            + "ProductId INT NOT NULL, "
                            + "QuantityOrdered INT NOT NULL, "
                            + "QuantityPicked INT NOT NULL DEFAULT 0, "
                            + "QuantityShipped INT NOT NULL DEFAULT 0, "
                            + "CONSTRAINT FK_OrderLines_Order FOREIGN KEY (OrderId) REFERENCES Orders(OrderId), "
                            + "CONSTRAINT FK_OrderLines_Product FOREIGN KEY (ProductId) REFERENCES Products(ProductId)"
                            + ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS PickRuns ("
                            + "PickRunId INT AUTO_INCREMENT PRIMARY KEY, "
                            + "AssignedToUserId INT NULL, "
                            + "StartedAt TIMESTAMP NULL, "
                            + "CompletedAt TIMESTAMP NULL, "
                            + "Status VARCHAR(20) NOT NULL DEFAULT 'Created', "
                            + "CONSTRAINT FK_PickRuns_User FOREIGN KEY (AssignedToUserId) REFERENCES Users(UserId), "
                            + "CONSTRAINT CK_PickRuns_Status CHECK (Status IN ('Created','InProgress','Completed','Cancelled'))"
                            + ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS PickRunItems ("
                            + "PickRunItemId INT AUTO_INCREMENT PRIMARY KEY, "
                            + "PickRunId INT NOT NULL, "
                            + "OrderLineId INT NOT NULL, "
                            + "BinId INT NOT NULL, "
                            + "QuantityToPick INT NOT NULL, "
                            + "QuantityPicked INT NOT NULL DEFAULT 0, "
                            + "Status VARCHAR(20) NOT NULL DEFAULT 'Pending', "
                            + "CONSTRAINT FK_PickRunItems_PickRun FOREIGN KEY (PickRunId) REFERENCES PickRuns(PickRunId), "
                            + "CONSTRAINT FK_PickRunItems_OrderLine FOREIGN KEY (OrderLineId) REFERENCES OrderLines(OrderLineId), "
                            + "CONSTRAINT FK_PickRunItems_Bin FOREIGN KEY (BinId) REFERENCES Bins(BinId), "
                            + "CONSTRAINT CK_PickRunItems_Status CHECK (Status IN ('Pending','Picked','ShortPicked','Skipped'))"
                            + ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS AuditLog ("
                            + "AuditId INT AUTO_INCREMENT PRIMARY KEY, "
                            + "TableName VARCHAR(128) NOT NULL, "
                            + "RecordId INT NOT NULL, "
                            + "ActionType VARCHAR(10) NOT NULL, "
                            + "ColumnName VARCHAR(128) NULL, "
                            + "OldValue CLOB NULL, "
                            + "NewValue CLOB NULL, "
                            + "ChangedByUserId INT NOT NULL, "
                            + "ChangedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                            + "CONSTRAINT FK_AuditLog_User FOREIGN KEY (ChangedByUserId) REFERENCES Users(UserId), "
                            + "CONSTRAINT CK_AuditLog_ActionType CHECK (ActionType IN ('INSERT','UPDATE','DELETE'))"
                            + ")"
            );

            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS IX_Products_Barcode ON Products(Barcode)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS IX_Inventory_ProductId ON Inventory(ProductId)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS IX_Inventory_BinId ON Inventory(BinId)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS IX_OrderLines_OrderId ON OrderLines(OrderId)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS IX_OrderLines_ProductId ON OrderLines(ProductId)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS IX_PickRunItems_PickRunId ON PickRunItems(PickRunId)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS IX_AuditLog_Table_Record ON AuditLog(TableName, RecordId)");

            String adminHash = UserRepository.hashPassword("admin123");
            try (PreparedStatement ps = conn.prepareStatement(
                    "MERGE INTO Users (Username, PasswordHash, FullName, Role) KEY(Username) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, "admin");
                ps.setString(2, adminHash);
                ps.setString(3, "System Administrator");
                ps.setString(4, "Admin");
                ps.executeUpdate();
            }
        }
    }
}

