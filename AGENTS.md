# AGENTS.md

Monolith backend for the Actracker project: Java 17, Gradle 8.5 (wrapper), Spring Boot 3.1, multi-module Gradle build (modules listed in `settings.gradle`).

## Commands

- Build + unit tests (what CI runs first): `./gradlew clean build test`
- Integration tests: `./gradlew integrationTest` — a **separate custom source set** (`src/integration-test/java`), NOT included in `build`. Runs `mustRunAfter test`.
- Single test: `./gradlew :actracker-api-domain:test --tests 'SomeTest'` or `./gradlew :actracker-api-rest:integrationTest --tests 'SomeTest'`
- Run app locally: `./gradlew bootRun` (main class `ovh.equino.actracker.main.springboot.ActrackerApi`). Context path is `/actracker-api`; actuator under `/actracker-api/info`.
- `./gradlew setupInfra` / `./gradlew tearDownInfra` — docker-compose up/down for Postgres + RabbitMQ. Only needed for `dev`/`int`/`prod` profiles; the default profile uses H2 + in-memory publisher and needs no infrastructure.
- No linter, formatter, or static analysis is configured. Don't go looking for a lint task.

## Gotchas

- **Credentials are required for any Gradle invocation**: `settings.gradle` and `build.gradle` unconditionally reference Gradle properties `mavenEquinoUsername` / `mavenEquinoPassword` for the private repo `maven.cloud.equino.ovh` (hosts the `ovh.equino.version` plugin and `ovh.equino.security` deps). Put them in `~/.gradle/gradle.properties` (CI passes them as `ORG_GRADLE_PROJECT_*` env vars).
- **Only `actracker-api-domain` has `src/test`.** Every other module's tests live in `src/integration-test` — if you can't find unit tests, that's expected, not a mistake.
- Integration tests in `actracker-api-db-postgres` use Testcontainers (`postgres:15.1`) and **require Docker**; `actracker-api-db-h2` integration tests use in-memory H2 and don't.
- CI is **Jenkins**, defined in `jenkins_files/Jenkinsfile_{pr,release,master}`. GitHub Actions workflows are deliberately parked in `._github/workflows` (renamed from `.github/` when Jenkins was enabled) so GitHub ignores them — don't "fix" the path.
- App version comes from the `ovh.equino.version` Gradle plugin: current version in `ovh.equino.version/initialVersion`, generated version in `build/ovh.equino.version/currentVersion`. PR pipeline runs `./gradlew verifyRelease` before building.

## Architecture

DDD-ish layering; dependency direction is always toward `actracker-api-domain`:

- `actracker-api-domain` — entities, factories, validators, and **ports** (`*DataSource`, `*Repository`, `*SearchEngine`, `*Notifier`, `*AccessibilityVerifier`). Framework-free: no Spring/Jakarta imports (keep it that way).
- `actracker-api-application` — use-case services + command/query/result DTOs. Also framework-free. `rest` depends only on this module.
- `actracker-api-rest` — Spring MVC controllers mapping HTTP ↔ application commands/queries (package `rest.spring.{activity,tag,tagset,dashboard,share}`).
- Adapters implementing domain ports: `repository-jpa` (writes), `datasource-jpa` (reads), `search-datasource`, `dashboard-generator-repository`, `notification-outbox` → `publisher-rabbitmq` / `publisher-memory`, `db-h2` / `db-postgres`, shared JPA plumbing in `jpa`.
- `actracker-api-main-springboot` is the **composition root**: all wiring happens there via `@Configuration` + `@Profile`. Profile names equal module names (e.g. `actracker-api-db-postgres`), grouped in `application.yml` (`default`, `dev`, `int`, `prod`).
- Most adapters are wired as `runtimeOnly` in `actracker-api-main-springboot/build.gradle`. **Adding a new adapter/module means editing that file**, or it won't be on the classpath.

## Database migrations (Flyway)

- Schema files: `actracker-api-db-postgres/src/main/resources/schema/V<n>__<name>.sql`.
- **Every new version must ship a matching `rollback/V<n>__<name>.sql`** — rollbacks are never executed automatically, they're run manually to restore a previous schema.
- Spring's Flyway auto-config is disabled (`spring.flyway.enabled: false`); migration runs programmatically at startup via `SchemaMigrator` in `actracker-api-db-postgres`.
- Manual migration (see `actracker-api-db-postgres/README.MD`): `./gradlew :actracker-api-db-postgres:flywayMigrate :actracker-api-db-postgres:flywayInfo -Pflyway.user=... -Pflyway.password=... -Pflyway.defaultSchema=... -Pflyway.url=jdbc:postgresql://localhost:5432/DB`
