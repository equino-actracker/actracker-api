package ovh.equino.actracker.repository.jpa;

import org.testcontainers.containers.PostgreSQLContainer;
import ovh.equino.actracker.postgres.SchemaMigrator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class IntegrationTestPostgresDataBase extends IntegrationTestRelationalDataBase {

    public static final IntegrationTestPostgresDataBase INSTANCE = new IntegrationTestPostgresDataBase();

    private final PostgreSQLContainer<?> container;

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String driverClassName;

    private IntegrationTestPostgresDataBase() {
        container = new PostgreSQLContainer<>("postgres:15.1");
        container.start();
        this.jdbcUrl = container.getJdbcUrl();
        this.username = container.getUsername();
        this.password = container.getPassword();
        this.driverClassName = container.getDriverClassName();
        migrateSchema();
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    public String jdbcUrl() {
        return jdbcUrl;
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public String password() {
        return password;
    }

    @Override
    public String driverClassName() {
        return driverClassName;
    }

    private void migrateSchema() {
        SchemaMigrator migrator = new SchemaMigrator(
                container.getJdbcUrl(),
                container.getUsername(),
                container.getPassword()
        );
        migrator.migrateSchema();
    }
}
