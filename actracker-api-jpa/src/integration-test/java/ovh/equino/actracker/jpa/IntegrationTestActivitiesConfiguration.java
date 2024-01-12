package ovh.equino.actracker.jpa;

import org.apache.commons.collections4.CollectionUtils;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.MetricValue;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.user.User;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static java.util.stream.Stream.concat;

public final class IntegrationTestActivitiesConfiguration {

    private final IntegrationTestTagsConfiguration tags;

    private final List<ActivityDto> addedActivities = new ArrayList<>();
    private final List<ActivityDto> transientActivities = new ArrayList<>();

    IntegrationTestActivitiesConfiguration(IntegrationTestTagsConfiguration tags) {
        this.tags = tags;
    }

    void persistIn(IntegrationTestRelationalDataBase database) throws SQLException {
        database.addActivities(addedActivities.toArray(new ActivityDto[0]));
    }

    public void add(ActivityDto activity) {
        addedActivities.add(activity);
    }

    public void addTransient(ActivityDto activity) {
        transientActivities.add(activity);
    }

    public List<ActivityDto> accessibleFor(User user) {
        return addedActivities
                .stream()
                .filter(not(ActivityDto::deleted))
                .filter(activity -> isOwnerOrGrantee(user, activity))
                .map(activity -> toAccessibleFormFor(user, activity))
                .sorted(comparing(activity -> activity.id().toString()))
                .toList();
    }

    public List<ActivityDto> accessibleForWithLimitOffset(User user, int limit, int offset) {
        return accessibleFor(user)
                .stream()
                .skip(offset)
                .limit(limit)
                .toList();
    }

    public List<ActivityDto> accessibleForExcluding(User user, Set<UUID> excludedIds) {
        return accessibleFor(user)
                .stream()
                .filter(activity -> !excludedIds.contains(activity.id()))
                .toList();
    }

    public List<ActivityDto> accessibleForContainingAnyOfTags(User user, Set<UUID> requiredTags) {
        return accessibleFor(user)
                .stream()
                .filter(activity -> CollectionUtils.containsAny(requiredTags, activity.tags()))
                .toList();
    }

    public List<ActivityDto> accessibleForInTimeRange(User user, Instant rangeStart, Instant rangeEnd) {
        List<ActivityDto> matchingActivities = accessibleFor(user);
        if (nonNull(rangeStart)) {
            matchingActivities = matchingActivities
                    .stream()
                    .filter(activity -> isNull(activity.endTime()) || !activity.endTime().isBefore(rangeStart))
                    .toList();
        }
        if (nonNull(rangeEnd)) {
            matchingActivities = matchingActivities
                    .stream()
                    .filter(activity -> isNull(activity.startTime()) || !activity.startTime().isAfter(rangeEnd))
                    .toList();
        }
        return matchingActivities;
    }

    public List<ActivityDto> accessibleOwnUnfinishedStartedBefore(User user, Instant startTime) {
        return accessibleFor(user)
                .stream()
                .filter(activity -> isOwner(user, activity))
                .filter(activity -> nonNull(activity.startTime()))
                .filter(activity -> isNull(activity.endTime()))
                .filter(activity -> activity.startTime().isBefore(startTime))
                .toList();
    }

    public List<ActivityDto> inaccessibleFor(User user) {
        List<UUID> accessibleActivities = accessibleFor(user)
                .stream()
                .map(ActivityDto::id)
                .toList();
        return concat(addedActivities.stream(), transientActivities.stream())
                .filter(activity -> !accessibleActivities.contains(activity.id()))
                .toList();
    }

    public Collection<UUID> flatTagIdsAccessibleFor(User user) {
        return accessibleFor(user)
                .stream()
                .flatMap(activity -> activity.tags().stream())
                .toList();
    }

    public Collection<MetricValue> flatMetricValuesAccessibleFor(User user) {
        return accessibleFor(user)
                .stream()
                .flatMap(activity -> activity.metricValues().stream())
                .toList();
    }

    private boolean isOwnerOrGrantee(User user, ActivityDto activity) {
        return isOwner(user, activity) || containsAnyTagAccessibleFor(user, activity);
    }

    private boolean isOwner(User user, ActivityDto activity) {
        return user.id().equals(activity.creatorId());
    }

    private boolean containsAnyTagAccessibleFor(User user, ActivityDto activity) {
        Set<UUID> accessibleTagIds = tags.accessibleFor(user)
                .stream()
                .map(TagDto::id)
                .collect(toUnmodifiableSet());
        return activity.tags()
                .stream()
                .anyMatch(accessibleTagIds::contains);
    }

    private ActivityDto toAccessibleFormFor(User user, ActivityDto activity) {
        return new ActivityDto(
                activity.id(),
                activity.creatorId(),
                activity.title(),
                activity.startTime(),
                activity.endTime(),
                activity.comment(),
                includedAccessibleTags(user, activity),
                includedAccessibleMetricValues(user, activity),
                activity.deleted()
        );
    }

    private List<MetricValue> includedAccessibleMetricValues(User user, ActivityDto activity) {
        Set<UUID> accessibleMetricIds = tags.accessibleFor(user)
                .stream()
                .flatMap(tag -> tag.metrics().stream())
                .map(MetricDto::id)
                .collect(toUnmodifiableSet());
        return activity.metricValues()
                .stream()
                .filter(metricValue -> accessibleMetricIds.contains(metricValue.metricId()))
                .toList();
    }

    private Set<UUID> includedAccessibleTags(User user, ActivityDto activity) {
        Set<UUID> accessibleTagIds = tags.accessibleFor(user)
                .stream()
                .map(TagDto::id)
                .collect(toUnmodifiableSet());
        return activity.tags()
                .stream()
                .filter(accessibleTagIds::contains)
                .collect(toUnmodifiableSet());
    }

}
