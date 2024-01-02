package ovh.equino.actracker.repository.jpa.activity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.activity.*;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.MetricId;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.*;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static ovh.equino.actracker.repository.jpa.TestUtil.nextUUID;

abstract class JpaActivityRepositoryIntegrationTest extends JpaIntegrationTest {

    private JpaActivityRepository repository;
    private User user;
    private ActivityFactory activityFactory;

    @BeforeEach
    void init() {
        this.user = new User(nextUUID());
        this.activityFactory = ActivityTestFactory.forUser(user);
        this.repository = new JpaActivityRepository(entityManager, activityFactory);
    }

    @Test
    void shouldAddAndGetMinimalActivity() {
        Activity expectedActivity = activityFactory.create(
                null,
                null,
                null,
                null,
                emptyList(),
                emptyList()
        );

        inTransaction(() -> repository.add(expectedActivity));
        inTransaction(() -> {
            Optional<Activity> foundActivity = repository.get(expectedActivity.id());
            assertThat(foundActivity).get().usingRecursiveComparison().isEqualTo(expectedActivity);
        });
    }

    @Test
    void shouldAddAndGetFullActivity() throws SQLException {
        TenantDto user = newUser().build();
        MetricDto metric = newMetric(user).build();
        TagDto tag = newTag(user).withMetrics(metric).build();
        database().addTags(tag);

        Activity expectedActivity = activityFactory.create(
                "activity title",
                Instant.ofEpochMilli(1),
                Instant.ofEpochMilli(2),
                "activity comment",
                List.of(new TagId(tag.id())),
                List.of(new MetricValue(metric.id(), TEN))
        );

        inTransaction(() -> repository.add(expectedActivity));
        inTransaction(() -> {
            Optional<Activity> foundActivity = repository.get(expectedActivity.id());
            assertThat(foundActivity).get().usingRecursiveComparison().isEqualTo(expectedActivity);
        });
    }

    @Test
    void shouldAddAndGetMutatedActivity() throws SQLException {
        TenantDto user = newUser().build();
        TagId tagToRemove = new TagId();
        MetricDto metricToSet = newMetric(user).build();
        MetricDto metricToUpdate = newMetric(user).build();
        MetricDto metricToUnset = newMetric(user).build();
        TagDto tagWithMetrics = newTag(user).withMetrics(metricToSet, metricToUpdate, metricToUnset).build();
        database().addTags(tagWithMetrics);

        Activity expectedActivity = activityFactory.create(
                "old activity title",
                Instant.ofEpochMilli(1),
                Instant.ofEpochMilli(2),
                "old activity comment",
                List.of(new TagId(tagWithMetrics.id())),
                List.of(new MetricValue(metricToUpdate.id(), TEN), new MetricValue(metricToUnset.id(), ONE))
        );

        expectedActivity.rename("new activity title");
        expectedActivity.finish(Instant.ofEpochMilli(4));
        expectedActivity.start(Instant.ofEpochMilli(3));
        expectedActivity.updateComment("new activity comment");
        expectedActivity.assignTag(new TagId());
        expectedActivity.removeTag(tagToRemove);
        expectedActivity.setMetricValue(new MetricValue(metricToSet.id(), ZERO));
        expectedActivity.setMetricValue(new MetricValue(metricToUpdate.id(), ONE));
        expectedActivity.unsetMetricValue(new MetricId(metricToUnset.id()));
        expectedActivity.delete();

        inTransaction(() -> repository.add(expectedActivity));
        inTransaction(() -> {
            Optional<Activity> foundActivity = repository.get(expectedActivity.id());
            assertThat(foundActivity).get().usingRecursiveComparison().isEqualTo(expectedActivity);
        });
    }

    @Test
    void shouldNotGetNotExistingActivity() {
        inTransaction(() -> {
            Optional<Activity> foundActivity = repository.get(new ActivityId(randomUUID()));
            assertThat(foundActivity).isEmpty();
        });
    }

    @Test
    void shouldUpdateActivity() throws SQLException {
        TenantDto user = newUser().build();
        MetricDto metric = newMetric(user).build();
        TagDto tag = newTag(user).withMetrics(metric).build();
        database().addTags(tag);

        Activity expectedActivity = activityFactory.create(
                "old title",
                null,
                null,
                null,
                emptyList(),
                emptyList()
        );

        inTransaction(() -> repository.add(expectedActivity));


        inTransaction(() -> {
            Activity activity = repository.get(expectedActivity.id()).get();

            expectedActivity.delete();
            activity.delete();

            repository.save(activity);
        });

        inTransaction(() -> {
            Optional<Activity> foundActivity = repository.get(expectedActivity.id());
            assertThat(foundActivity).get().usingRecursiveComparison().isEqualTo(expectedActivity);
        });
    }
}
