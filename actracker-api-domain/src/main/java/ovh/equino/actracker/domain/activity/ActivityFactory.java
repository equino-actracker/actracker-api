package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.tag.MetricsAccessibilityVerifier;
import ovh.equino.actracker.domain.tag.TagDataSource;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.Collection;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
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

        var activitiesAccessibilityVerifier = new ActivitiesAccessibilityVerifier(activityDataSource, creator);
        var tagsAccessibilityVerifier = new TagsAccessibilityVerifier(tagDataSource, creator);
        var metricsAccessibilityVerifier = new MetricsAccessibilityVerifier(tagDataSource, creator);
        var validator = new ActivityValidator(tagsAccessibilityVerifier, metricsAccessibilityVerifier);

        var activity = new Activity(
                new ActivityId(),
                creator,
                title,
                startTime,
                endTime,
                comment,
                requireNonNullElse(tags, emptyList()),
                requireNonNullElse(metricValues, emptyList()),
                !DELETED,
                activitiesAccessibilityVerifier,
                tagsAccessibilityVerifier,
                metricsAccessibilityVerifier,
                validator
        );
        activity.validate();
        return activity;
    }

    public Activity reconstitute(User actor,
                                 ActivityId id,
                                 User creator,
                                 String title,
                                 Instant startTime,
                                 Instant endTime,
                                 String comment,
                                 Collection<TagId> tags,
                                 Collection<MetricValue> metricValues,
                                 boolean deleted) {

        var activitiesAccessibilityVerifier = new ActivitiesAccessibilityVerifier(activityDataSource, actor);
        var tagsAccessibilityVerifier = new TagsAccessibilityVerifier(tagDataSource, actor);
        var metricsAccessibilityVerifier = new MetricsAccessibilityVerifier(tagDataSource, actor);
        var validator = new ActivityValidator(tagsAccessibilityVerifier, metricsAccessibilityVerifier);

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
}
