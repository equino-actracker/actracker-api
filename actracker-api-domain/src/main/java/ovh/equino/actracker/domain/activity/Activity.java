package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;

class Activity implements Entity {

    private final ActivityId id;
    private final User creator;
    private Instant startTime;
    private Instant endTime;
    private String comment;
    private final Set<TagId> tags;
    private boolean deleted;

    private Activity(
            ActivityId id,
            User creator,
            Instant startTime,
            Instant endTime,
            String comment,
            Collection<TagId> tags,
            boolean deleted) {

        this.id = requireNonNull(id);
        this.creator = requireNonNull(creator);
        this.startTime = startTime;
        this.endTime = endTime;
        this.comment = comment;
        this.tags = new HashSet<>(tags);
        this.deleted = deleted;
    }

    static Activity create(ActivityDto activity, User creator) {
        Activity newActivity = new Activity(
                new ActivityId(),
                creator,
                activity.startTime(),
                activity.endTime(),
                activity.comment(),
                toTagIds(activity),
                false
        );
        newActivity.validate();
        return newActivity;
    }

    void updateTo(ActivityDto activity) {
        this.startTime = activity.startTime();
        this.endTime = activity.endTime();
        this.comment = activity.comment();
        this.tags.addAll(toTagIds(activity));
        validate();
    }

    void delete() {
        this.deleted = true;
    }

    static Activity fromStorage(ActivityDto activity) {
        return new Activity(
                new ActivityId(activity.id()),
                new User(activity.creatorId()),
                activity.startTime(),
                activity.endTime(),
                activity.comment(),
                toTagIds(activity),
                activity.deleted()
        );
    }

    ActivityDto forStorage() {
        Set<UUID> tagIds = tags.stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());
        return new ActivityDto(id.id(), creator.id(), startTime, endTime, comment, tagIds, deleted);
    }

    ActivityDto forClient(TagsExistenceVerifier tagsExistenceVerifier) {
        Set<UUID> tagIds = tagsExistenceVerifier.existing(tags).stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());

        return new ActivityDto(id.id(), creator.id(), startTime, endTime, comment, tagIds, deleted);
    }

    ActivityChangedNotification forChangeNotification() {
        Set<UUID> tagIds = tags.stream()
                .map(TagId::id)
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

    private static List<TagId> toTagIds(ActivityDto activity) {
        return requireNonNullElse(activity.tags(), new HashSet<UUID>()).stream()
                .map(TagId::new)
                .toList();
    }
}
