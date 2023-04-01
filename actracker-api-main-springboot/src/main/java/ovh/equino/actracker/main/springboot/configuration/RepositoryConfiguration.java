package ovh.equino.actracker.main.springboot.configuration;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import ovh.equino.actracker.domain.activity.ActivityRepository;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.domain.tenant.TenantRepository;
import ovh.equino.actracker.notification.outbox.NotificationsOutboxRepository;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.repository.jpa",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        ActivityRepository.class,
                        TagRepository.class,
                        TenantRepository.class,
                        NotificationsOutboxRepository.class
                }
        )
)
class RepositoryConfiguration {

    @Bean("entityManagerFactory")
    LocalSessionFactoryBean sessionFactory(@Qualifier("applicationDataSource") DataSource dataSource,
                                           Properties hibernateProperties) {

        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan("ovh.equino.actracker.repository.jpa");
        sessionFactory.setHibernateProperties(hibernateProperties);
        return sessionFactory;
    }

    @Bean
    Properties hibernateProperties(@Qualifier("hibernateDialect") String hibernateDialect) {
        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.dialect", hibernateDialect);
        hibernateProperties.setProperty("hibernate.show_sql", "false");
        hibernateProperties.setProperty("hibernate.format_sql", "false");
        hibernateProperties.setProperty("hibernate.use_sql_comments", "false");
        return hibernateProperties;
    }

    @Bean
    HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory);
        return transactionManager;
    }
}
