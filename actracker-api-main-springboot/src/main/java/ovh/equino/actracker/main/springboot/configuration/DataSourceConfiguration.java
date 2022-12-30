package ovh.equino.actracker.main.springboot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import javax.sql.DataSource;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

@Configuration
class DataSourceConfiguration {

    @Bean
    DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(H2)
                .addScript("h2Schema.sql")
                .build();
    }

    @Bean("hibernateDialect")
    String hibernateDialect() {
        return "org.hibernate.dialect.H2Dialect";
    }
}
