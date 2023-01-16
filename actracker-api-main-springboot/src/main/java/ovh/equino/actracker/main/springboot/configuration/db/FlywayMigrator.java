package ovh.equino.actracker.main.springboot.configuration.db;

import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Profile({"actracker-api-db-postgres"})
class FlywayMigrator {

    @Autowired
    @Qualifier("ownerDataSource")
    private DataSource ownerDataSource;

    @PostConstruct
    void migrateSchema() {
        Flyway flyway = Flyway.configure()
                .dataSource(ownerDataSource)
                .locations("classpath:schema/")
                .load();

        flyway.migrate();
    }
}
