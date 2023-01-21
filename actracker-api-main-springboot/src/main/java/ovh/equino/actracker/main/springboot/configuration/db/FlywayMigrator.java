package ovh.equino.actracker.main.springboot.configuration.db;

import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

class FlywayMigrator {

    private final DataSource ownerDataSource;

    FlywayMigrator(DataSource ownerDataSource) {
        this.ownerDataSource = ownerDataSource;
    }

    @PostConstruct
    void migrateSchema() {
        Flyway flyway = Flyway.configure()
                .dataSource(ownerDataSource)
                .locations("classpath:schema/")
                .load();

        flyway.migrate();
    }
}
