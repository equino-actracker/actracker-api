package ovh.equino.actracker.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.jpa.HibernatePersistenceProvider;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.MetricValue;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.MetricType;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toUnmodifiableSet;

public abstract class IntegrationTestBase {

    protected static final IntegrationTestRelationalDataBase DATABASE = new IntegrationTestPostgres();
    private final EntityManagerFactory entityManagerFactory;

    protected IntegrationTestBase() {
        this.entityManagerFactory = new HibernatePersistenceProvider().createContainerEntityManagerFactory(
                new PersistenceUnitInfo(),
                persistenceProperties()
        );
    }

    protected EntityManager entityManager() {
        return entityManagerFactory.createEntityManager();
    }

    protected void inTransaction(EntityManager entityManager, Execution execution) {
        entityManager.getTransaction().begin();
        execution.call();
        entityManager.getTransaction().commit();
    }

    protected TenantDto newUser() {
        return new TenantDto(randomUUID(), randomString(), randomString());
    }

    protected ActivityDto newActivity(TenantDto creator) {

        Set<TagDto> tags = Set.of(
                newTag(creator),
                newTag(creator)
        );
        Set<UUID> tagIds = tags
                .stream()
                .map(TagDto::id)
                .collect(toUnmodifiableSet());
        List<MetricValue> metricValues = tags
                .stream()
                .map(TagDto::metrics)
                .flatMap(Collection::stream)
                .map(metric -> new MetricValue(metric.id(), randomBigDecimal()))
                .toList();

        return new ActivityDto(randomUUID(),
                creator.id(),
                randomUUID().toString(),
                Instant.ofEpochSecond(1),
                Instant.ofEpochSecond(2),
                randomUUID().toString(),
                tagIds,
                metricValues,
                false
        );
    }

    protected TagSetDto newTagSet(TenantDto creator) {

        Set<TagDto> tags = Set.of(
                newTag(creator),
                newTag(creator)
        );
        Set<UUID> tagIds = tags
                .stream()
                .map(TagDto::id)
                .collect(toUnmodifiableSet());

        return new TagSetDto(
                randomUUID(),
                creator.id(),
                randomString(),
                tagIds,
                false
        );
    }

    protected TagDto newTag(TenantDto creator) {
        TenantDto grantee1 = new TenantDto(randomUUID(), randomString(), randomString());
        TenantDto grantee2 = new TenantDto(randomUUID(), randomString(), randomString());

        return new TagDto(
                randomUUID(),
                creator.id(),
                randomString(),
                List.of(
                        new MetricDto(randomUUID(), creator.id(), randomString(), MetricType.NUMERIC, false),
                        new MetricDto(randomUUID(), creator.id(), randomString(), MetricType.NUMERIC, false),
                        new MetricDto(randomUUID(), creator.id(), randomString(), MetricType.NUMERIC, false)
                ),
                List.of(
                        new Share(new User(grantee1.id()), grantee1.username()),
                        new Share(new User(grantee2.id()), grantee2.username())
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
