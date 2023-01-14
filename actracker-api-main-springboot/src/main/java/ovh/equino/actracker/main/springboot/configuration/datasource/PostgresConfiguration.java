package ovh.equino.actracker.main.springboot.configuration.datasource;

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
    private String postgresUsername;

    @Value("${actracker-api-db-postgres.app.password:postgres}")
    private String postgresPassword;

    @Value("${actracker-api-db-postgres.app.host:localhost}")
    private String postgresHost;

    @Value("${actracker-api-db-postgres.app.port:5432}")
    private String postgresPort;

    @Value("${actracker-api-db-postgres.app.dbName:postgres}")
    private String postgresDbName;

    @Value("${actracker-api-db-postgres.app.schema:public}")
    private String postgresSchema;

    @Bean
    DataSource remoteDataSource() {
        String postgresUrl = "jdbc:postgresql://%s:%s/%s".formatted(postgresHost, postgresPort, postgresDbName);
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(postgresUrl);
        dataSource.setUsername(postgresUsername);
        dataSource.setPassword(postgresPassword);
        dataSource.setSchema(postgresSchema);
        return dataSource;
    }

    @Bean("hibernateDialect")
    String hibernateDialect() {
        return "org.hibernate.dialect.PostgreSQLDialect";
    }
}
