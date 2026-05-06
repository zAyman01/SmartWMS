# SmartWMS

Skeleton Java desktop application for a warehouse management system.

## Quick start

```bash
mvn -q -DskipTests compile
```

Run `com.warehousewms.App` from your IDE to launch the Swing placeholder.

## Database check

Run `com.warehousewms.DbConnectionCheck` from your IDE. It defaults to H2.
To force SQL Server, set the JVM property `-Dwms.useSqlServer=true`.
The runner prints the DB URL, verifies the `Users` table, and checks the admin seed.

## Tests

```bash
mvn -q test
```

## Notes

- Database connectivity is configured in `com.warehousewms.config`.
- UI screens will live under `com.warehousewms.ui`.


