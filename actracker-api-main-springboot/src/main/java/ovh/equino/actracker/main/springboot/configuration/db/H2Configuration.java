package ovh.equino.actracker.main.springboot.configuration.db;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import javax.sql.DataSource;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

@Configuration
@Profile("actracker-api-db-h2")
class H2Configuration {

    @Bean("applicationDataSource")
    DataSource applicationDataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(H2)
                .addScript("h2Schema.sql")
                .addScript("h2Data.sql")
                .build();
    }

    @Bean("hibernateDialect")
    String hibernateDialect() {
        return "org.hibernate.dialect.H2Dialect";
    }
}
