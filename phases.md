## Project phases and checklist

### Phase 1 — Project setup (DONE)
- [x] Maven project structure created
- [x] Java 17 compiler configured
- [x] Core dependencies added (SQL Server, HikariCP, H2, FlatLaf, SLF4J)

### Phase 2 — Database connection (DONE)
- [x] Connection pools for SQL Server and H2
- [x] Database selection and fallback logic
- [x] H2 schema aligned with SQL Server script
- [x] Default admin user seeded in H2
- [x] Externalize DB credentials (properties file/env)
- [x] Add SQL Server schema script to repo (e.g., `db/SmartWMS.sql`)
- [x] Add DB smoke test runner and document usage
- [x] Add automated tests for connection + schema + fallback

### Phase 3 — Auth and user management (DONE)
- [x] Login UI + validation flow
- [x] Registration UI + validation flow
- [x] Password hashing
- [x] User CRUD UI
- [x] Password reset flow
- [x] Role-based access control

### Phase 4 — Master data (DONE)
- [x] Products CRUD
- [x] Suppliers CRUD
- [x] Customers CRUD
- [x] Bins/location hierarchy UI

### Phase 5 — Inventory operations (DONE)
- [x] Receiving (PO -> receipt -> inventory)
- [x] Picking/packing/shipping
- [x] Stock adjustments and transfers

### Phase 6 — Reporting & dashboards (PLANNED)
- [ ] KPI dashboard (JFreeChart)
- [ ] Printable reports (JasperReports)

### Phase 7 — Polish & deployment (PLANNED)
- [ ] Error handling and logging
- [ ] Installer/build packaging
- [ ] Backup/restore strategy