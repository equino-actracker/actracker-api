package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toUnmodifiableSet;

class Activity implements Entity {

    private final ActivityId id;
    private final User creator;
    private Instant startTime;
    private Instant endTime;
    private String comment;
    private Set<TagId> tags;
    private boolean deleted;

    Activity(
            ActivityId newId,
            ActivityDto activityData,
            User creator) {

        this.id = requireNonNull(newId);
        this.creator = requireNonNull(creator);
        this.startTime = activityData.startTime();
        this.endTime = activityData.endTime();
        this.comment = activityData.comment();
        this.tags = tagsData(activityData);
        this.deleted = false;
        validate();
    }

    private Set<TagId> tagsData(ActivityDto activityData) {
        return requireNonNullElse(activityData.tags(), new HashSet<UUID>()).stream()
                .map(TagId::new)
                .collect(toCollection(HashSet::new));
    }

    static Activity fromDto(ActivityDto activityData) {
        Activity activity = new Activity(
                new ActivityId(activityData.id()),
                activityData,
                new User(activityData.creatorId())
        );
        activity.deleted = activityData.deleted();

        return activity;
    }

    void updateTo(ActivityDto activity) {
        startTime = activity.startTime();
        endTime = activity.endTime();
        comment = activity.comment();
        tags = tagsData(activity);
        validate();
    }

    boolean isAvailableFor(User user) {
        return creator.equals(user);
    }

    boolean isNotAvailableFor(User user) {
        return !isAvailableFor(user);
    }

    void delete() {
        this.deleted = true;
    }

    @Override
    public void validate() {
        new ActivityValidator(this).validate();
    }

    ActivityDto toDto() {
        Set<UUID> tagIds = tags.stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());
        return new ActivityDto(id.id(), creator.id(), startTime, endTime, comment, tagIds, deleted);
    }

    ActivityChangedNotification toChangeNotification() {
        return new ActivityChangedNotification(this.toDto());
    }

    Instant startTime() {
        return startTime;
    }

    Instant endTime() {
        return endTime;
    }

}
