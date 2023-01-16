package ovh.equino.actracker.main.springboot.configuration.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@Profile("actracker-api-db-postgres")
class PostgresConfiguration {

    @Value("${actracker-api-db-postgres.app.username:postgres}")
    private String applicationUsername;

    @Value("${actracker-api-db-postgres.app.password:postgres}")
    private String applicationPassword;

    @Value("${actracker-api-db-postgres.owner.username:postgres}")
    private String ownerUsername;

    @Value("${actracker-api-db-postgres.owner.password:postgres}")
    private String ownerPassword;

    @Value("${actracker-api-db-postgres.host:localhost}")
    private String host;

    @Value("${actracker-api-db-postgres.port:5432}")
    private String port;

    @Value("${actracker-api-db-postgres.dbName:postgres}")
    private String dbName;

    @Value("${actracker-api-db-postgres.schema:public}")
    private String schemaName;

    private String url() {
        return "jdbc:postgresql://%s:%s/%s".formatted(host, port, dbName);
    }

    @Bean("applicationDataSource")
    DataSource applicationDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url());
        dataSource.setUsername(applicationUsername);
        dataSource.setPassword(applicationPassword);
        dataSource.setSchema(schemaName);
        return dataSource;
    }

    @Bean("ownerDataSource")
    DataSource ownerDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url());
        dataSource.setUsername(ownerUsername);
        dataSource.setPassword(ownerPassword);
        dataSource.setSchema(schemaName);
        return dataSource;
    }

    @Bean("hibernateDialect")
    String hibernateDialect() {
        return "org.hibernate.dialect.PostgreSQLDialect";
    }
}
