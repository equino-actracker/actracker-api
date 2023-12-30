package ovh.equino.actracker.repository.jpa.activity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.activity.*;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.TEN;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static ovh.equino.actracker.repository.jpa.TestUtil.nextUUID;
import static ovh.equino.actracker.repository.jpa.TestUtil.randomBigDecimal;

abstract class JpaActivityRepositoryIntegrationTest extends JpaIntegrationTest {

    private JpaActivityRepository repository;
    private ActivityFactory activityFactory;

    @BeforeEach
    void init() {
        User user = new User(nextUUID());
        this.activityFactory = ActivityTestFactory.forUser(user);
        this.repository = new JpaActivityRepository(entityManager, activityFactory);
    }

    @Test
    void shouldAddAndFindActivity() throws SQLException {
        TenantDto user = newUser().build();
        MetricDto metric1 = newMetric(user).build();
        MetricDto metric2 = newMetric(user).build();
        TagDto tag1 = newTag(user).withMetrics(metric1).build();
        TagDto tag2 = newTag(user).withMetrics(metric2).build();
        database().addTags(tag1, tag2);

        ActivityDto newActivity = newActivity(user)
                .withTags(tag1, tag2)
                .withMetricValues(
                        new MetricValue(metric1.id(), randomBigDecimal()),
                        new MetricValue(metric2.id(), randomBigDecimal())
                )
                .build();

        inTransaction(() -> {
            repository.add(newActivity);
            Optional<ActivityDto> foundActivity = repository.findById(newActivity.id());
            assertThat(foundActivity).get().usingRecursiveComparison().isEqualTo(newActivity);
        });

        inTransaction(() -> {
            Optional<ActivityDto> foundActivity = repository.findById(newActivity.id());
            assertThat(foundActivity).get().usingRecursiveComparison().isEqualTo(newActivity);
        });
    }

    @Test
    void shouldNotFindNotExistingActivity() {
        inTransaction(() -> {
            Optional<ActivityDto> foundActivity = repository.findById(randomUUID());
            assertThat(foundActivity).isEmpty();
        });
    }

    @Test
    void shouldAddAndGetActivity() throws SQLException {
        TenantDto user = newUser().build();
        MetricDto metric = newMetric(user).build();
        TagDto tag = newTag(user).withMetrics(metric).build();
        database().addTags(tag);

        Activity expectedActivity = activityFactory.create(
                "title",
                Instant.ofEpochMilli(1),
                Instant.ofEpochMilli(2),
                "comment",
                List.of(new TagId(tag.id())),
                List.of(new MetricValue(metric.id(), randomBigDecimal()))
        );

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
            var newTitle = "new title";
            var newStartTime = Instant.ofEpochMilli(10);
            var newEndTime = Instant.ofEpochMilli(20);
            var newComment = "new comment";
            var newTag = new TagId(tag.id());
            var newMetricValue = new MetricValue(metric.id(), TEN);

            Activity activity = repository.get(expectedActivity.id()).get();

            expectedActivity.rename(newTitle);
            expectedActivity.start(newStartTime);
            expectedActivity.finish(newEndTime);
            expectedActivity.updateComment(newComment);
            expectedActivity.assignTag(newTag);
            expectedActivity.setMetricValue(newMetricValue);
            expectedActivity.delete();

            activity.rename(newTitle);
            activity.start(newStartTime);
            activity.finish(newEndTime);
            activity.updateComment(newComment);
            activity.assignTag(newTag);
            activity.setMetricValue(newMetricValue);
            activity.delete();

            repository.save(activity);
        });
        inTransaction(() -> {
            Optional<Activity> foundActivity = repository.get(expectedActivity.id());
            assertThat(foundActivity).get().usingRecursiveComparison().isEqualTo(expectedActivity);
        });
    }
}
