package ovh.equino.actracker.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.jpa.HibernatePersistenceProvider;
import ovh.equino.actracker.domain.tenant.TenantDto;

import java.util.HashMap;
import java.util.Map;

public abstract class IntegrationTestBase {

    protected static final int LARGE_PAGE_SIZE = 1000;
    protected static final String FIRST_PAGE = "";

    protected static final IntegrationTestRelationalDataBase DATABASE = new IntegrationTestPostgres();
    protected final EntityManager entityManager;

    protected IntegrationTestBase() {
        EntityManagerFactory entityManagerFactory = new HibernatePersistenceProvider()
                .createContainerEntityManagerFactory(
                        new PersistenceUnitInfo(),
                        persistenceProperties()
                );
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    protected void inTransaction(Execution execution) {
        entityManager.getTransaction().begin();
        execution.call();
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

    protected static DashboardBuilder newDashboard(TenantDto creator) {
        return new DashboardBuilder(creator);
    }

    private Map<String, String> persistenceProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", DATABASE.jdbcUrl());
        properties.put("javax.persistence.jdbc.user", DATABASE.username());
        properties.put("javax.persistence.jdbc.password", DATABASE.password());
        properties.put("javax.persistence.jdbc.driver", DATABASE.driverClassName());
        return properties;
    }
}
