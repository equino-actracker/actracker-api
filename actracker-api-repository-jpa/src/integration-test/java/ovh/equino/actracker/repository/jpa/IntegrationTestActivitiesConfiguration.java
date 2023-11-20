package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.MetricValue;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.user.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Comparator.comparing;
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

    public List<ActivityDto> inaccessibleFor(User user) {
        List<UUID> accessibleActivities = accessibleFor(user)
                .stream()
                .map(ActivityDto::id)
                .toList();
        return concat(addedActivities.stream(), transientActivities.stream())
                .filter(activity -> !accessibleActivities.contains(activity.id()))
                .toList();
    }

    private boolean isOwnerOrGrantee(User user, ActivityDto activity) {
        return isOwner(user, activity);// || isGrantee(user, activity);
    }

    private boolean isOwner(User user, ActivityDto activity) {
        return user.id().equals(activity.creatorId());
    }

//    private boolean isGrantee(User user, ActivityDto activity) {
//        List<User> grantees = activity.shares()
//                .stream()
//                .map(Share::grantee)
//                .toList();
//        return grantees.contains(user);
//    }

    private ActivityDto toAccessibleFormFor(User user, ActivityDto activity) {
        List<UUID> accessibleTagIds = tags.accessibleFor(user)
                .stream()
                .map(TagDto::id)
                .toList();
        Set<UUID> includedAccessibleTags = activity.tags()
                .stream()
                .filter(accessibleTagIds::contains)
                .collect(toUnmodifiableSet());
        Set<UUID> accessibleMetricIds = tags.accessibleFor(user)
                .stream()
                .flatMap(tag -> tag.metrics().stream())
                .map(MetricDto::id)
                .collect(toUnmodifiableSet());
        List<MetricValue> includedAccessibleMetricValues = activity.metricValues()
                .stream()
                .filter(metricValue -> accessibleMetricIds.contains(metricValue.metricId()))
                .toList();
        return new ActivityDto(
                activity.id(),
                activity.creatorId(),
                activity.title(),
                activity.startTime(),
                activity.endTime(),
                activity.comment(),
                includedAccessibleTags,
                includedAccessibleMetricValues,
                activity.deleted()
        );
    }
}
