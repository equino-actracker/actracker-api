package ovh.equino.actracker.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.jpa.HibernatePersistenceProvider;
import ovh.equino.actracker.domain.tenant.TenantDto;

import java.util.HashMap;
import java.util.Map;

public abstract class JpaIntegrationTest {

    protected static final int LARGE_PAGE_SIZE = 1000;
    protected static final String FIRST_PAGE = "";

    protected final EntityManager entityManager;

    protected JpaIntegrationTest() {
        EntityManagerFactory entityManagerFactory = new HibernatePersistenceProvider()
                .createContainerEntityManagerFactory(
                        new PersistenceUnitInfo(),
                        persistenceProperties()
                );
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    protected abstract IntegrationTestRelationalDataBase database();

    protected void inTransaction(TransactionalOperation transactionalOperation) {
        entityManager.getTransaction().begin();
        transactionalOperation.execute();
        entityManager.getTransaction().commit();
    }

    protected static TenantBuilder newUser() {
        return new TenantBuilder();
    }

    protected static ActivityBuilder newActivity(TenantDto creator) {
        return new ActivityBuilder(creator);
    }

    protected static TagSetBuilder newTagSet(TenantDto creator) {
        return new TagSetBuilder(creator);
    }

    protected static TagBuilder newTag(TenantDto creator) {
        return new TagBuilder(creator);
    }

    protected static MetricBuilder newMetric(TenantDto creator) {
        return new MetricBuilder(creator);
    }

    protected static DashboardBuilder newDashboard(TenantDto creator) {
        return new DashboardBuilder(creator);
    }

    protected static ChartBuilder newChart(TenantDto creator) {
        return new ChartBuilder(creator);
    }

    private Map<String, String> persistenceProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", database().jdbcUrl());
        properties.put("javax.persistence.jdbc.user", database().username());
        properties.put("javax.persistence.jdbc.password", database().password());
        properties.put("javax.persistence.jdbc.driver", database().driverClassName());
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        return properties;
    }
}
