package ovh.equino.actracker.domain.activity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.tag.MetricId;
import ovh.equino.actracker.domain.tag.MetricsAccessibilityVerifier;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.User;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityEditOperationTest {

    private static final User CREATOR = new User(randomUUID());
    private static final boolean DELETED = true;

    @Mock
    private TagsAccessibilityVerifier tagsAccessibilityVerifier;
    @Mock
    private MetricsAccessibilityVerifier metricsAccessibilityVerifier;
    @Mock
    private ActivityValidator validator;

    @Test
    void shouldPreserveAssignedNotExistingTags() {
        // given
        TagId existingTagId = new TagId(randomUUID());
        TagId nonExistingTagId = new TagId(randomUUID());
        when(tagsAccessibilityVerifier.accessibleOf(any()))
                .thenReturn(singleton(existingTagId));
        when(tagsAccessibilityVerifier.nonAccessibleOf(any()))
                .thenReturn(singleton(nonExistingTagId));

        Activity activity = new Activity(
                new ActivityId(randomUUID()),
                CREATOR,
                null,
                null,
                null,
                null,
                List.of(existingTagId, nonExistingTagId),
                emptyList(),
                !DELETED,
                tagsAccessibilityVerifier,
                metricsAccessibilityVerifier,
                validator
        );
        ActivityEditOperation editOperation = new ActivityEditOperation(
                CREATOR, activity, tagsAccessibilityVerifier, metricsAccessibilityVerifier, () -> {
        });

        // when
        editOperation.beforeEditOperation();
        List<TagId> tagsDuringEdit = activity.tags.stream().toList();
        editOperation.afterEditOperation();

        // then
        assertThat(tagsDuringEdit).containsExactly(existingTagId);
        assertThat(activity.tags).containsExactlyInAnyOrder(existingTagId, nonExistingTagId);
    }

    @Test
    void shouldPreserveValuesOfNotExistingMetrics() {
        // given
        MetricId existingMetricId = new MetricId(randomUUID());
        MetricValue valueOfExistingMetric = new MetricValue(existingMetricId.id(), BigDecimal.ONE);
        MetricId nonExistingMetricId = new MetricId(randomUUID());
        MetricValue valueOfNonExistingMetric = new MetricValue(nonExistingMetricId.id(), BigDecimal.ZERO);
        when(metricsAccessibilityVerifier.accessibleOf(any(), any()))
                .thenReturn(singleton(existingMetricId));
        when(metricsAccessibilityVerifier.nonAccessibleOf(any(), any()))
                .thenReturn(singleton(nonExistingMetricId));

        Activity activity = new Activity(
                new ActivityId(randomUUID()),
                CREATOR,
                null,
                null,
                null,
                null,
                emptyList(),
                List.of(valueOfExistingMetric, valueOfNonExistingMetric),
                !DELETED,
                tagsAccessibilityVerifier,
                metricsAccessibilityVerifier,
                validator
        );
        ActivityEditOperation editOperation = new ActivityEditOperation(
                CREATOR, activity, tagsAccessibilityVerifier, metricsAccessibilityVerifier, () -> {
        });

        // when
        editOperation.beforeEditOperation();
        List<MetricValue> valuesDuringEdit = activity.metricValues.stream().toList();
        editOperation.afterEditOperation();

        // then
        assertThat(valuesDuringEdit).containsExactly(valueOfExistingMetric);
        assertThat(activity.metricValues).containsExactlyInAnyOrder(valueOfExistingMetric, valueOfNonExistingMetric);
    }
}