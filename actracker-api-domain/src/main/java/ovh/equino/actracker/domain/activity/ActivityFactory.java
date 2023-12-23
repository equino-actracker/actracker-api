package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.requireNonNullElse;

public final class ActivityFactory {

    private static final Boolean DELETED = TRUE;

    private final ActivityDataSource activityDataSource;
    private final TagDataSource tagDataSource;

    ActivityFactory(ActivityDataSource activityDataSource, TagDataSource tagDataSource) {
        this.activityDataSource = activityDataSource;
        this.tagDataSource = tagDataSource;
    }

    public Activity create(User creator,
                           String title,
                           Instant startTime,
                           Instant endTime,
                           String comment,
                           Collection<TagId> tags,
                           Collection<MetricValue> metricValues) {

        // TODO verifiers should be injected
        var activitiesAccessibilityVerifier = new ActivitiesAccessibilityVerifier(activityDataSource);
        var tagsAccessibilityVerifier = new TagsAccessibilityVerifier(tagDataSource);
        var metricsAccessibilityVerifier = new MetricsAccessibilityVerifier(tagDataSource);
        var validator = new ActivityValidator();

        var nonNullTags = requireNonNullElse(tags, new ArrayList<TagId>());
        var nonNullMetricValues = requireNonNullElse(metricValues, new ArrayList<MetricValue>());
        validateTagsAccessibleFor(creator, nonNullTags, tagsAccessibilityVerifier);
        validateMetricsAccessibleFor(creator, nonNullMetricValues, nonNullTags, metricsAccessibilityVerifier);

        var activity = new Activity(
                new ActivityId(),
                creator,
                title,
                startTime,
                endTime,
                comment,
                nonNullTags,
                nonNullMetricValues,
                !DELETED,
                activitiesAccessibilityVerifier,
                tagsAccessibilityVerifier,
                metricsAccessibilityVerifier,
                validator
        );
        activity.validate();
        return activity;
    }

    public Activity reconstitute(User actor,    // TODO remove
                                 ActivityId id,
                                 User creator,
                                 String title,
                                 Instant startTime,
                                 Instant endTime,
                                 String comment,
                                 Collection<TagId> tags,
                                 Collection<MetricValue> metricValues,
                                 boolean deleted) {

        // TODO verifiers should be injected
        var activitiesAccessibilityVerifier = new ActivitiesAccessibilityVerifier(activityDataSource);
        var tagsAccessibilityVerifier = new TagsAccessibilityVerifier(tagDataSource);
        var metricsAccessibilityVerifier = new MetricsAccessibilityVerifier(tagDataSource);
        var validator = new ActivityValidator();

        return new Activity(
                id,
                creator,
                title,
                startTime,
                endTime,
                comment,
                tags,
                metricValues,
                deleted,
                activitiesAccessibilityVerifier,
                tagsAccessibilityVerifier,
                metricsAccessibilityVerifier,
                validator
        );
    }

    // TODO when verifier will be an injected field, parameter not required
    private void validateTagsAccessibleFor(
            User user,
            Collection<TagId> tags,
            TagsAccessibilityVerifier tagsAccessibilityVerifier) {

        tagsAccessibilityVerifier.nonAccessibleFor(user, tags)
                .stream()
                .findFirst()
                .ifPresent((inaccessibleTag) -> {
                    String errorMessage = "Tag with ID %s not found".formatted(inaccessibleTag.id());
                    throw new EntityInvalidException(Activity.class, errorMessage);
                });
    }

    // TODO when verifier will be an injected field, parameter not required
    private void validateMetricsAccessibleFor(User user,
                                              Collection<MetricValue> metricValues,
                                              Collection<TagId> tags,
                                              MetricsAccessibilityVerifier metricsAccessibilityVerifier) {

        var setMetrics = metricValues
                .stream()
                .map(MetricValue::metricId)
                .map(MetricId::new)
                .toList();
        metricsAccessibilityVerifier.nonAccessibleFor(user, setMetrics, tags)
                .stream()
                .findFirst()
                .ifPresent((inaccessibleMetric) -> {
                    String errorMessage = "Metric with ID %s not found".formatted(inaccessibleMetric.id());
                    throw new EntityInvalidException(Activity.class, errorMessage);
                });
    }
}
