package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.tag.MetricId;
import ovh.equino.actracker.domain.tag.MetricsAccessibilityVerifier;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import static java.util.Collections.emptySet;

public class ActivityTestFactory implements ActivityFactory {

    private final User user;
    private final ActorExtractor actorExtractor;
    private final ActivitiesAccessibilityVerifier activitiesAccessibilityVerifier;
    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;
    private final MetricsAccessibilityVerifier metricsAccessibilityVerifier;
    private final ActivityValidator activityValidator;

    public static ActivityFactory forUser(User user) {
        return new ActivityTestFactory(user);
    }

    private ActivityTestFactory(User user) {
        this.user = user;
        this.actorExtractor = () -> user;
        this.activitiesAccessibilityVerifier = (user1, activityId) -> true;
        this.tagsAccessibilityVerifier = new TagsAccessibilityVerifier() {
            @Override
            public boolean isAccessibleFor(User user, TagId tag) {
                return true;
            }

            @Override
            public Set<TagId> nonAccessibleFor(User user, Collection<TagId> tags) {
                return emptySet();
            }
        };
        this.metricsAccessibilityVerifier = new MetricsAccessibilityVerifier() {
            @Override
            public boolean isAccessibleFor(User user, MetricId metric, Collection<TagId> tags) {
                return true;
            }

            @Override
            public Set<MetricId> nonAccessibleFor(User user, Collection<MetricId> metrics, Collection<TagId> tags) {
                return emptySet();
            }
        };
        this.activityValidator = new ActivityValidator();
    }

    @Override
    public Activity create(String title,
                           Instant startTime,
                           Instant endTime,
                           String comment,
                           Collection<TagId> tags,
                           Collection<MetricValue> metricValues) {

        return new Activity(
                new ActivityId(),
                user,
                title,
                startTime,
                endTime,
                comment,
                tags,
                metricValues,
                false,
                actorExtractor,
                activitiesAccessibilityVerifier,
                tagsAccessibilityVerifier,
                metricsAccessibilityVerifier,
                activityValidator
        );
    }

    @Override
    public Activity reconstitute(ActivityId id,
                                 User creator,
                                 String title,
                                 Instant startTime,
                                 Instant endTime,
                                 String comment,
                                 Collection<TagId> tags,
                                 Collection<MetricValue> metricValues,
                                 boolean deleted) {

        return new Activity(
                id,
                user,
                title,
                startTime,
                endTime,
                comment,
                tags,
                metricValues,
                deleted,
                actorExtractor,
                activitiesAccessibilityVerifier,
                tagsAccessibilityVerifier,
                metricsAccessibilityVerifier,
                activityValidator
        );
    }
}
