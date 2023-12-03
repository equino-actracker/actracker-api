package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.EntityValidator;
import ovh.equino.actracker.domain.tag.MetricId;
import ovh.equino.actracker.domain.tag.MetricsAccessibilityVerifier;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;

import java.time.Instant;
import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

class ActivityValidator extends EntityValidator<Activity> {

    // TODO validate dependencies during create and edit before removing from here
    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;
    // TODO validate dependencies during create and edit before removing from here
    private final MetricsAccessibilityVerifier metricsAccessibilityVerifier;

    ActivityValidator(TagsAccessibilityVerifier tagsAccessibilityVerifier, MetricsAccessibilityVerifier metricsAccessibilityVerifier) {
        this.tagsAccessibilityVerifier = tagsAccessibilityVerifier;
        this.metricsAccessibilityVerifier = metricsAccessibilityVerifier;
    }

    @Override
    protected Class<Activity> entityType() {
        return Activity.class;
    }

    @Override
    protected List<String> collectValidationErrors(Activity activity) {
        List<String> validationErrors = new LinkedList<>();

        checkEndTimeBeforeStartTime(activity).ifPresent(validationErrors::add);
        checkContainsNonExistingTags(activity).ifPresent(validationErrors::add);
        checkContainsValuesOfNonExistingMetrics(activity).ifPresent(validationErrors::add);

        return validationErrors;
    }

    private Optional<String> checkEndTimeBeforeStartTime(Activity activity) {
        if (endTimeBeforeStartTime(activity)) {
            return Optional.of("End time is before start time");
        }
        return Optional.empty();
    }

    private boolean endTimeBeforeStartTime(Activity activity) {
        Instant activityStartTime = activity.startTime();
        Instant activityEndTime = activity.endTime();

        if (activityStartTime == null || activityEndTime == null) {
            return false;
        }
        return activityEndTime.isBefore(activityStartTime);
    }

    private Optional<String> checkContainsNonExistingTags(Activity activity) {
        Set<TagId> notExistingTags = tagsAccessibilityVerifier.nonAccessibleOf(activity.tags());
        if (isEmpty(notExistingTags)) {
            return Optional.empty();
        }
        List<UUID> notExistingTagIds = notExistingTags.stream()
                .map(TagId::id)
                .toList();
        return Optional.of("Selected tags do not exist: %s".formatted(notExistingTagIds));
    }

    private Optional<String> checkContainsValuesOfNonExistingMetrics(Activity activity) {

        Set<MetricId> notExistingMetrics = metricsAccessibilityVerifier.nonAccessibleOf(
                activity.selectedMetrics(),
                activity.tags()
        );
        if (isEmpty(notExistingMetrics)) {
            return Optional.empty();
        }
        List<UUID> notExistingMetricIds = notExistingMetrics.stream()
                .map(MetricId::id)
                .toList();
        return Optional.of("Selected metrics do not exist in selected tags: %s".formatted(notExistingMetricIds));
    }
}
