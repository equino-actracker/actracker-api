package ovh.equino.actracker.domain.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ovh.equino.actracker.domain.tag.MetricType.NUMERIC;

@ExtendWith(MockitoExtension.class)
class MetricsAccessibilityVerifierTest {

    private static final User USER = new User(randomUUID());
    private static final boolean DELETED = true;

    private static final MetricDto ACCESSIBLE_METRIC_1 = metric("accessible metric 1");
    private static final MetricDto ACCESSIBLE_METRIC_2 = metric("accessible metric 2");
    private static final MetricDto INACCESSIBLE_METRIC_1 = metric("inaccessible metric 1");
    private static final MetricDto INACCESSIBLE_METRIC_2 = metric("inaccessible metric 2");

    private static final TagDto ACCESSIBLE_TAG_1 = tag("accessible tag 1", ACCESSIBLE_METRIC_1);
    private static final TagDto ACCESSIBLE_TAG_2 = tag("accessible tag 2", ACCESSIBLE_METRIC_2);
    private static final TagDto INACCESSIBLE_TAG_1 = tag("inaccessible tag 1", INACCESSIBLE_METRIC_1);
    private static final TagDto INACCESSIBLE_TAG_2 = tag("inaccessible tag 2", INACCESSIBLE_METRIC_2);

    private static final MetricId ACCESSIBLE_METRIC_1_ID = new MetricId(ACCESSIBLE_METRIC_1.id());
    private static final MetricId ACCESSIBLE_METRIC_2_ID = new MetricId(ACCESSIBLE_METRIC_2.id());
    private static final MetricId INACCESSIBLE_METRIC_1_ID = new MetricId(INACCESSIBLE_METRIC_1.id());
    private static final MetricId INACCESSIBLE_METRIC_2_ID = new MetricId(INACCESSIBLE_METRIC_2.id());

    private static final TagId ACCESSIBLE_TAG_1_ID = new TagId(ACCESSIBLE_TAG_1.id());
    private static final TagId ACCESSIBLE_TAG_2_ID = new TagId(ACCESSIBLE_TAG_2.id());
    private static final TagId INACCESSIBLE_TAG_1_ID = new TagId(INACCESSIBLE_TAG_1.id());
    private static final TagId INACCESSIBLE_TAG_2_ID = new TagId(INACCESSIBLE_TAG_2.id());

    @Mock
    private TagDataSource tagDataSource;

    private MetricsAccessibilityVerifier metricsAccessibilityVerifier;

    @BeforeEach
    void init() {
        metricsAccessibilityVerifier = new MetricsAccessibilityVerifier(tagDataSource);
    }

    @Test
    void shouldFindInaccessibleMetrics() {
        // given
        when(tagDataSource.find(any(Set.class), any(User.class)))
                .thenReturn(List.of(ACCESSIBLE_TAG_1, ACCESSIBLE_TAG_2));

        // when
        Set<MetricId> inaccessibleMetrics = metricsAccessibilityVerifier.nonAccessibleFor(
                USER,
                List.of(
                        ACCESSIBLE_METRIC_1_ID,
                        ACCESSIBLE_METRIC_2_ID,
                        INACCESSIBLE_METRIC_1_ID,
                        INACCESSIBLE_METRIC_2_ID
                ),
                List.of(
                        ACCESSIBLE_TAG_1_ID,
                        ACCESSIBLE_TAG_2_ID,
                        INACCESSIBLE_TAG_1_ID,
                        INACCESSIBLE_TAG_2_ID
                )
        );

        // then
        assertThat(inaccessibleMetrics).containsExactlyInAnyOrder(
                INACCESSIBLE_METRIC_1_ID,
                INACCESSIBLE_METRIC_2_ID
        );
    }

    @Test
    void shouldConfirmMetricAccessible() {
        // given
        when(tagDataSource.find(any(Set.class), any(User.class)))
                .thenReturn(List.of(ACCESSIBLE_TAG_1, ACCESSIBLE_TAG_2));

        // when
        boolean isAccessible = metricsAccessibilityVerifier.isAccessibleFor(
                USER,
                ACCESSIBLE_METRIC_1_ID,
                List.of(
                        ACCESSIBLE_TAG_1_ID,
                        ACCESSIBLE_TAG_2_ID,
                        INACCESSIBLE_TAG_1_ID,
                        INACCESSIBLE_TAG_2_ID
                )
        );

        // then
        assertThat(isAccessible).isTrue();
    }

    @Test
    void shouldConfirmMetricInaccessible() {
        // given
        when(tagDataSource.find(any(Set.class), any(User.class)))
                .thenReturn(emptyList());

        // when
        boolean isAccessible = metricsAccessibilityVerifier.isAccessibleFor(
                USER,
                INACCESSIBLE_METRIC_1_ID,
                List.of(
                        ACCESSIBLE_TAG_1_ID,
                        ACCESSIBLE_TAG_2_ID,
                        INACCESSIBLE_TAG_1_ID,
                        INACCESSIBLE_TAG_2_ID
                )
        );

        // then
        assertThat(isAccessible).isFalse();
    }

    private static MetricDto metric(String name) {
        return new MetricDto(
                randomUUID(),
                randomUUID(),
                name,
                NUMERIC,
                !DELETED
        );
    }

    private static TagDto tag(String name, MetricDto metric) {
        return new TagDto(
                randomUUID(),
                randomUUID(),
                name,
                singletonList(metric),
                emptyList(),
                !DELETED
        );
    }
}
