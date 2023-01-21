# Running manual flyway schema migration:

Schema is migrated automatically on application startup.
However, it's recommended to migrate schema manually, when the migration is expected to be risky or time consuming.

To integrate with Equino Cloud Postgres instance, it's require to forward port to Kubernetes postgres service:
`kubectl port-forward services/actracker-dev -n equino-postgres 5432:5432`

After that, in the project root directory it's required to run:
`./gradlew :actracker-api-db-postgres:flywayMigrate :actracker-api-db-postgres:flywayInfo -Pflyway.user=USER -Pflyway.password=PASSWORD -Pflyway.defaultSchema=SCHEMA_NAME -Pflyway.url=jdbc:postgresql://localhost:5432/DB_NAME`
It's required to run as database owner user.