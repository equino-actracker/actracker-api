package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.*;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;

class Activity implements Entity {

    private final ActivityId id;
    private final User creator;
    private String title;
    private Instant startTime;
    private Instant endTime;
    private String comment;
    private final Set<TagId> tags;
    private boolean deleted;

    private final TagsExistenceVerifier tagsExistenceVerifier;

    private Activity(
            ActivityId id,
            User creator,
            String title,
            Instant startTime,
            Instant endTime,
            String comment,
            Collection<TagId> tags,
            boolean deleted,
            TagsExistenceVerifier tagsExistenceVerifier) {

        this.id = requireNonNull(id);
        this.creator = requireNonNull(creator);
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.comment = comment;
        this.tags = new HashSet<>(tags);
        this.deleted = deleted;
        this.tagsExistenceVerifier = tagsExistenceVerifier;
    }

    static Activity create(ActivityDto activity, User creator, TagsExistenceVerifier tagsExistenceVerifier) {
        Activity newActivity = new Activity(
                new ActivityId(),
                creator,
                activity.title(),
                activity.startTime(),
                activity.endTime(),
                activity.comment(),
                toTagIds(activity),
                false,
                tagsExistenceVerifier
        );
        newActivity.validate();
        return newActivity;
    }

    void updateTo(ActivityDto activity) {
        Set<TagId> deletedAssignedTags = tagsExistenceVerifier.notExisting(this.tags);
        this.title = activity.title();
        this.startTime = activity.startTime();
        this.endTime = activity.endTime();
        this.comment = activity.comment();
        this.tags.clear();
        this.tags.addAll(toTagIds(activity));
        validate();
        this.tags.addAll(deletedAssignedTags);
    }

    void delete() {
        this.deleted = true;
    }

    static Activity fromStorage(ActivityDto activity, TagsExistenceVerifier tagsExistenceVerifier) {
        return new Activity(
                new ActivityId(activity.id()),
                new User(activity.creatorId()),
                activity.title(),
                activity.startTime(),
                activity.endTime(),
                activity.comment(),
                toTagIds(activity),
                activity.deleted(),
                tagsExistenceVerifier
        );
    }

    ActivityDto forStorage() {
        Set<UUID> tagIds = tags.stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());
        return new ActivityDto(id.id(), creator.id(), title, startTime, endTime, comment, tagIds, deleted);
    }

    ActivityDto forClient() {
        Set<UUID> tagIds = tagsExistenceVerifier.existing(tags).stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());

        return new ActivityDto(id.id(), creator.id(), title, startTime, endTime, comment, tagIds, deleted);
    }

    ActivityChangedNotification forChangeNotification() {
        Set<UUID> tagIds = tags.stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());
        ActivityDto dto = new ActivityDto(id.id(), creator.id(), title, startTime, endTime, comment, tagIds, deleted);
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
        new ActivityValidator(this, tagsExistenceVerifier).validate();
    }

    Instant startTime() {
        return startTime;
    }

    Instant endTime() {
        return endTime;
    }

    Set<TagId> tags() {
        return unmodifiableSet(tags);
    }

    private static List<TagId> toTagIds(ActivityDto activity) {
        return requireNonNullElse(activity.tags(), new HashSet<UUID>()).stream()
                .map(TagId::new)
                .toList();
    }
}
