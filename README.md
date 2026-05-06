# SmartWMS

Warehouse Management System (WMS) desktop application built with Java 17 and Swing. The project provides a SQL Server-backed production setup with an H2 offline fallback, plus a clean skeleton for future UI and domain modules.

## Status at a glance

- Phase 1 (Project setup): DONE
- Phase 2 (Database connection): DONE
- Phases 3–7: PLANNED

See `phases.md` for the detailed checklist.

## Features implemented

- SQL Server + H2 connection pooling via HikariCP
- H2 schema aligned with the SQL Server script (tables, constraints, indexes)
- Database selection and fallback logic
- Smoke test runner for connection + schema verification
- Automated tests for H2 schema and SQL Server fallback

## Tech stack

- Java 17
- Maven
- Swing (UI)
- HikariCP (connection pool)
- SQL Server JDBC driver
- H2 (offline fallback)
- JUnit 5 (tests)

## Project structure

```
SmartWMS/
├── src/
│   ├── main/
│   │   ├── java/com/warehousewms
│   │   │   ├── App.java
│   │   │   ├── DbConnectionCheck.java
│   │   │   ├── config/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   ├── ui/
│   │   │   └── viewmodel/
│   │   └── resources/
│   │       ├── db.properties
│   │       └── db/SmartWMS.sql
│   └── test/
│       ├── java/com/warehousewms/config
│       └── resources/db.properties
└── phases.md
```

## Configuration

### Database properties

Edit `src/main/resources/db.properties` to point to SQL Server and H2.

```
# SQL Server
# db.sqlserver.host=localhost
# db.sqlserver.port=1433
# db.sqlserver.database=SmartWMS
# db.sqlserver.user=sa
# db.sqlserver.password=1234

# H2 local fallback
# db.h2.url=jdbc:h2:file:./wms-local;AUTO_SERVER=TRUE
```

### SQL Server schema

The schema is stored at `src/main/resources/db/SmartWMS.sql`. Run it on your SQL Server instance to initialize the database.

## Running the app

Run `com.warehousewms.App` from your IDE. It currently launches a placeholder UI and is ready for screens under `com.warehousewms.ui`.

## Database smoke test

Run `com.warehousewms.DbConnectionCheck` from your IDE.

- Defaults to H2
- To force SQL Server, set `-Dwms.useSqlServer=true`
- Outputs the database URL, checks the `Users` table, and confirms the admin seed

## Automated tests

```
# Compile only
mvn -q -DskipTests compile

# Run tests
mvn -q test
```

## How database selection works

- If `-Dwms.useSqlServer=true`, the system attempts SQL Server first.
- If SQL Server is unreachable, it falls back to H2 automatically.
- H2 initialization is performed on startup for the fallback path.

## Extending the project

### Next steps (recommended)

- Build Login UI and wire to `AuthService`
- Add CRUD screens for Products, Suppliers, Customers, and Bins
- Implement inventory receiving and picking workflows
- Add dashboards and reporting

### Suggested module responsibilities

- `config`: connection pool, config loading, schema init
- `model`: POJOs representing database entities
- `repository`: SQL access layer (CRUD)
- `service`: business logic
- `ui`: Swing forms and screens
- `viewmodel`: UI state and validation

## Troubleshooting

- If SQL Server connection fails, verify host/port, credentials, and that the database exists.
- Use `DbConnectionCheck` to verify schema initialization and fallback behavior.
- Ensure `db.properties` is on the classpath and correctly formatted.

## License

Internal project, no public license defined.
