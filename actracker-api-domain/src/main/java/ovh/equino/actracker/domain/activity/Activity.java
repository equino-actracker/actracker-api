package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.tag.Tag;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;

class Activity implements Entity {

    private final ActivityId id;
    private final User creator;
    private Instant startTime;
    private Instant endTime;
    private String comment;
    private final Set<Tag> tags = new HashSet<>();
    private boolean deleted;

    private Activity(
            ActivityId id,
            User creator,
            Instant startTime,
            Instant endTime,
            String comment,
            boolean deleted) {

        this.id = requireNonNull(id);
        this.creator = requireNonNull(creator);
        this.startTime = startTime;
        this.endTime = endTime;
        this.comment = comment;
        this.deleted = deleted;
    }

    static Activity create(ActivityDto activity, User creator) {
        Activity newActivity = new Activity(
                new ActivityId(),
                creator,
                activity.startTime(),
                activity.endTime(),
                activity.comment(),
                false
        );
        newActivity.validate();
        return newActivity;
    }

    void updateTo(ActivityDto activity) {
        startTime = activity.startTime();
        endTime = activity.endTime();
        comment = activity.comment();
        validate();
    }

    void delete() {
        this.deleted = true;
    }

    static Activity fromStorage(ActivityDto activityData) {
        return new Activity(
                new ActivityId(activityData.id()),
                new User(activityData.creatorId()),
                activityData.startTime(),
                activityData.endTime(),
                activityData.comment(),
                activityData.deleted()
        );
    }

    ActivityDto forStorage() {
        Set<UUID> tagIds = tags.stream()
                .map(tag -> tag.id().id())
                .collect(toUnmodifiableSet());
        return new ActivityDto(id.id(), creator.id(), startTime, endTime, comment, tagIds, deleted);
    }

    ActivityDto forClient() {
        Set<UUID> tagIds = tags.stream()
                .filter(Tag::isNotDeleted)
                .map(tag -> tag.id().id())
                .collect(toUnmodifiableSet());
        return new ActivityDto(id.id(), creator.id(), startTime, endTime, comment, tagIds, deleted);
    }

    ActivityChangedNotification forChangeNotification() {
        Set<UUID> tagIds = tags.stream()
                .map(tag -> tag.id().id())
                .collect(toUnmodifiableSet());
        ActivityDto dto = new ActivityDto(id.id(), creator.id(), startTime, endTime, comment, tagIds, deleted);
        return new ActivityChangedNotification(dto);
    }

    boolean isAvailableFor(User user) {
        return creator.equals(user);
    }

    boolean isNotAvailableFor(User user) {
        return !isAvailableFor(user);
    }

    @Override
    public void validate() {
        new ActivityValidator(this).validate();
    }

    Instant startTime() {
        return startTime;
    }

    Instant endTime() {
        return endTime;
    }

}
