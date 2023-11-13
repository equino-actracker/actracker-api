package ovh.equino.actracker.repository.jpa.activity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.MetricValue;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.sql.SQLException;
import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static ovh.equino.actracker.repository.jpa.TestUtil.randomBigDecimal;

abstract class JpaActivityRepositoryIntegrationTest extends JpaIntegrationTest {

    private JpaActivityRepository repository;

    @BeforeEach
    void init() {
        this.repository = new JpaActivityRepository(entityManager);
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
}
