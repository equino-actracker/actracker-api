package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.EntityEditOperation;
import ovh.equino.actracker.domain.EntityModification;
import ovh.equino.actracker.domain.tag.MetricId;
import ovh.equino.actracker.domain.tag.MetricsAccessibilityVerifier;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toUnmodifiableSet;

class ActivityEditOperation extends EntityEditOperation<Activity> {

    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;
    private final MetricsAccessibilityVerifier metricsAccessibilityVerifier;
    private Set<TagId> tagsToPreserve;
    private List<MetricValue> metricValuesToPreserve;

    protected ActivityEditOperation(User editor,
                                    Activity entity,
                                    TagsAccessibilityVerifier tagsAccessibilityVerifier,
                                    MetricsAccessibilityVerifier metricsAccessibilityVerifier,
                                    EntityModification entityModification) {

        super(editor, entity, entityModification);
        this.tagsAccessibilityVerifier = tagsAccessibilityVerifier;
        this.metricsAccessibilityVerifier = metricsAccessibilityVerifier;
    }

    @Override
    protected void beforeEditOperation() {
        preserveTags();
        preserveMetricValues();
    }

    private void preserveTags() {
        this.tagsToPreserve = tagsAccessibilityVerifier.nonAccessibleOf(entity.tags);
        Set<TagId> existingTags = tagsAccessibilityVerifier.accessibleOf(entity.tags);
        entity.tags.clear();
        entity.tags.addAll(existingTags);
    }

    private void preserveMetricValues() {
        Set<MetricId> assignedMetricIds = entity.metricValues.stream()
                .map(MetricValue::metricId)
                .map(MetricId::new)
                .collect(toUnmodifiableSet());
        Set<UUID> notExistingAssignedMetricIds = metricsAccessibilityVerifier.nonAccessibleOf(assignedMetricIds, entity.tags)
                .stream()
                .map(MetricId::id)
                .collect(toUnmodifiableSet());
        Set<UUID> existingAssignedMetricIds = metricsAccessibilityVerifier.accessibleOf(assignedMetricIds, entity.tags)
                .stream()
                .map(MetricId::id)
                .collect(toUnmodifiableSet());
        metricValuesToPreserve = entity.metricValues.stream()
                .filter(metricValue -> notExistingAssignedMetricIds.contains(metricValue.metricId()))
                .toList();
        List<MetricValue> valuesOfExistingMetrics = entity.metricValues.stream()
                .filter(metricValue -> existingAssignedMetricIds.contains(metricValue.metricId()))
                .toList();

        entity.metricValues.clear();
        entity.metricValues.addAll(valuesOfExistingMetrics);
    }

    @Override
    protected void afterEditOperation() {
        entity.tags.addAll(tagsToPreserve);
        entity.metricValues.addAll(metricValuesToPreserve);
    }
}
