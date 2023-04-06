package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.tag.Tag;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.Collection;
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
    private final Set<Tag> tags;
    private boolean deleted;

    private Activity(
            ActivityId id,
            User creator,
            Instant startTime,
            Instant endTime,
            String comment,
            Collection<Tag> tags,
            boolean deleted) {

        this.id = requireNonNull(id);
        this.creator = requireNonNull(creator);
        this.startTime = startTime;
        this.endTime = endTime;
        this.comment = comment;
        this.tags = new HashSet<>(tags);
        this.deleted = deleted;
    }

    static Activity create(ActivityDto activity, User creator, Collection<Tag> tags) {
        Activity newActivity = new Activity(
                new ActivityId(),
                creator,
                activity.startTime(),
                activity.endTime(),
                activity.comment(),
                tags,
                false
        );
        newActivity.validate();
        return newActivity;
    }

    void updateTo(ActivityDto activity, Collection<Tag> tags) {
        this.startTime = activity.startTime();
        this.endTime = activity.endTime();
        this.comment = activity.comment();
        this.tags.removeIf(Tag::isNotDeleted);
        this.tags.addAll(tags);
        validate();
    }

    void delete() {
        this.deleted = true;
    }

    static Activity fromStorage(ActivityDto activity, Collection<Tag> tags) {
        return new Activity(
                new ActivityId(activity.id()),
                new User(activity.creatorId()),
                activity.startTime(),
                activity.endTime(),
                activity.comment(),
                tags,
                activity.deleted()
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
