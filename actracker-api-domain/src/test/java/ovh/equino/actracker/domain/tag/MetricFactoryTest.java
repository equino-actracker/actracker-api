package ovh.equino.actracker.domain.tag;

import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.user.User;

import static java.lang.Boolean.TRUE;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static ovh.equino.actracker.domain.tag.MetricType.NUMERIC;

class MetricFactoryTest {

    private static final MetricId METRIC_ID = new MetricId();
    private static final User CREATOR = new User(randomUUID());
    private static final String METRIC_NAME = "metric name";
    private static final Boolean DELETED = TRUE;

    private final MetricFactory metricFactory = new MetricFactory();

    @Test
    void shouldCreateMetric() {
        // when
        Metric metric = metricFactory.create(CREATOR, METRIC_NAME, NUMERIC);

        // then
        assertThat(metric.id()).isNotNull();
        assertThat(metric.creator()).isEqualTo(CREATOR);
        assertThat(metric.type()).isEqualTo(NUMERIC);
        assertThat(metric.deleted()).isFalse();
    }

    @Test
    void shouldReconstituteMetric() {
        // when
        Metric metric = metricFactory.reconstitute(METRIC_ID, CREATOR, METRIC_NAME, NUMERIC, DELETED);

        // then
        assertThat(metric.id()).isEqualTo(METRIC_ID);
        assertThat(metric.creator()).isEqualTo(CREATOR);
        assertThat(metric.name()).isEqualTo(METRIC_NAME);
        assertThat(metric.type()).isEqualTo(NUMERIC);
        assertThat(metric.deleted()).isTrue();
    }
}