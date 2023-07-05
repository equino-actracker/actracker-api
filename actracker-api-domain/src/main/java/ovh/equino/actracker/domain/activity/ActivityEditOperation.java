package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.EntityEditOperation;
import ovh.equino.actracker.domain.EntityModification;
import ovh.equino.actracker.domain.tag.MetricId;
import ovh.equino.actracker.domain.tag.MetricsExistenceVerifier;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toUnmodifiableSet;

class ActivityEditOperation extends EntityEditOperation<Activity> {

    private final TagsExistenceVerifier tagsExistenceVerifier;
    private final MetricsExistenceVerifier metricsExistenceVerifier;
    private Set<TagId> tagsToPreserve;
    private List<MetricValue> metricValuesToPreserve;

    protected ActivityEditOperation(User editor,
                                    Activity entity,
                                    TagsExistenceVerifier tagsExistenceVerifier,
                                    MetricsExistenceVerifier metricsExistenceVerifier,
                                    EntityModification entityModification) {

        super(editor, entity, entityModification);
        this.tagsExistenceVerifier = tagsExistenceVerifier;
        this.metricsExistenceVerifier = metricsExistenceVerifier;
    }

    @Override
    protected void beforeEditOperation() {
        preserveTags();
        preserveMetricValues();
    }

    private void preserveTags() {
        this.tagsToPreserve = tagsExistenceVerifier.notExisting(entity.tags);
        Set<TagId> existingTags = tagsExistenceVerifier.existing(entity.tags);
        entity.tags.clear();
        entity.tags.addAll(existingTags);
    }

    private void preserveMetricValues() {
        Set<MetricId> assignedMetricIds = entity.metricValues.stream()
                .map(MetricValue::metricId)
                .map(MetricId::new)
                .collect(toUnmodifiableSet());
        Set<UUID> notExistingAssignedMetricIds = metricsExistenceVerifier.notExisting(entity.tags, assignedMetricIds)
                .stream()
                .map(MetricId::id)
                .collect(toUnmodifiableSet());
        Set<UUID> existingAssignedMetricIds = metricsExistenceVerifier.existing(entity.tags, assignedMetricIds)
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
