package ovh.equino.actracker.postgres;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

import static org.flywaydb.core.Flyway.configure;

public class SchemaMigrator {

    private static final String SCHEMA_LOCATION = "classpath:schema/";

    private final Flyway flyway;

    public SchemaMigrator(DataSource ownerDataSource) {
        this.flyway = configure()
                .dataSource(ownerDataSource)
                .locations(SCHEMA_LOCATION)
                .load();
    }

    public SchemaMigrator(String jdbcUrl, String username, String password) {
        this.flyway = configure()
                .dataSource(jdbcUrl, username, password)
                .locations(SCHEMA_LOCATION)
                .load();
    }

    public void migrateSchema() {
        flyway.migrate();
    }
}
