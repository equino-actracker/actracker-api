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


//        inTransaction(() -> {
        Activity activity = repository.get(expectedActivity.id()).get();

        expectedActivity.delete();
        activity.delete();

        repository.save(activity);
//        });

//        inTransaction(() -> {
        Optional<Activity> foundActivity = repository.get(expectedActivity.id());
        assertThat(foundActivity).get().usingRecursiveComparison().isEqualTo(expectedActivity);
//        });
    }
}
