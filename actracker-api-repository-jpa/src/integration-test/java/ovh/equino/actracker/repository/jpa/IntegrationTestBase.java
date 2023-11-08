package ovh.equino.actracker.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.jpa.HibernatePersistenceProvider;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.MetricValue;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.ChartId;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.GroupBy;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.domain.dashboard.AnalysisMetric.TAG_DURATION;
import static ovh.equino.actracker.domain.dashboard.AnalysisMetric.TAG_PERCENTAGE;

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

    protected ActivityDto newActivity(TenantDto creator) {
        return new ActivityDto(randomUUID(),
                creator.id(),
                randomUUID().toString(),
                Instant.ofEpochSecond(1),
                Instant.ofEpochSecond(2),
                randomUUID().toString(),
                Set.of(randomUUID(), randomUUID(), randomUUID()),
                List.of(
                        new MetricValue(randomUUID(), randomBigDecimal()),
                        new MetricValue(randomUUID(), randomBigDecimal()),
                        new MetricValue(randomUUID(), randomBigDecimal())
                ),
                false
        );
    }

    protected static TagSetBuilder newTagSet(TenantDto creator) {
        return new TagSetBuilder(creator);
    }

    protected static TagBuilder newTag(TenantDto creator) {
        return new TagBuilder(creator);
    }

    protected DashboardDto newDashboard(TenantDto creator) {
        Chart chart1 = new Chart(
                new ChartId(randomUUID()),
                randomString(),
                GroupBy.SELF,
                TAG_PERCENTAGE,
                Set.of(randomUUID(), randomUUID(), randomUUID()),
                false
        );
        Chart chart2 = new Chart(
                new ChartId(randomUUID()),
                randomString(),
                GroupBy.DAY,
                TAG_DURATION,
                Set.of(randomUUID(), randomUUID(), randomUUID()),
                false
        );

        return new DashboardDto(
                randomUUID(),
                creator.id(),
                randomString(),
                List.of(chart1, chart2),
                List.of(
                        new Share(new User(randomUUID()), randomString()),
                        new Share(new User(randomUUID()), randomString()),
                        new Share(new User(randomUUID()), randomString())
                ),
                false
        );
    }

    private Map<String, String> persistenceProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", DATABASE.jdbcUrl());
        properties.put("javax.persistence.jdbc.user", DATABASE.username());
        properties.put("javax.persistence.jdbc.password", DATABASE.password());
        properties.put("javax.persistence.jdbc.driver", DATABASE.driverClassName());
        return properties;
    }

    protected String randomString() {
        int length = 10;
        boolean useLetters = true;
        boolean useNumbers = false;
        return RandomStringUtils.random(length, useLetters, useNumbers);
    }

    protected BigDecimal randomBigDecimal() {
        int length = 3;
        boolean useLetters = false;
        boolean useNumbers = true;
        return new BigDecimal(RandomStringUtils.random(length, useLetters, useNumbers));
    }
}
