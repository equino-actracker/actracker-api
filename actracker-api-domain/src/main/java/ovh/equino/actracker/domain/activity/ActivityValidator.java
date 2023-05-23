package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.EntityValidator;
import ovh.equino.actracker.domain.tag.MetricId;
import ovh.equino.actracker.domain.tag.MetricsExistenceVerifier;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

final class ActivityValidator extends EntityValidator<Activity> {

    private final Activity activity;
    private final TagsExistenceVerifier tagsExistenceVerifier;
    private final MetricsExistenceVerifier metricsExistenceVerifier;

    ActivityValidator(Activity activity, TagsExistenceVerifier tagsExistenceVerifier) {
        this.activity = activity;
        this.tagsExistenceVerifier = tagsExistenceVerifier;
        this.metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);
    }

    @Override
    protected List<String> collectValidationErrors() {
        List<String> validationErrors = new LinkedList<>();

        if (endTimeBeforeStartTime()) {
            validationErrors.add("End time is before start time");
        }

        Set<TagId> notExistingTags = tagsExistenceVerifier.notExisting(activity.tags());
        if (isNotEmpty(notExistingTags)) {
            List<UUID> notExistingTagIds = notExistingTags.stream()
                    .map(TagId::id)
                    .toList();
            validationErrors.add("Selected tags do not exist: %s".formatted(notExistingTagIds));
        }

        Set<MetricId> notExistingMetrics = metricsExistenceVerifier.notExisting(
                activity.tags(),
                activity.selectedMetrics()
        );
        if (isNotEmpty(notExistingMetrics)) {
            List<UUID> notExistingMetricIds = notExistingMetrics.stream()
                    .map(MetricId::id)
                    .toList();
            validationErrors.add("Selected metrics do not exist in selected tags: %s".formatted(notExistingMetricIds));
        }

        return validationErrors;
    }

    private boolean endTimeBeforeStartTime() {
        Instant activityStartTime = activity.startTime();
        Instant activityEndTime = activity.endTime();

        if (activityStartTime == null || activityEndTime == null) {
            return false;
        }
        return activityEndTime.isBefore(activityStartTime);
    }

    @Override
    protected Class<Activity> entityType() {
        return Activity.class;
    }
}
