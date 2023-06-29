package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.exception.EntityEditForbidden;
import ovh.equino.actracker.domain.tag.MetricId;
import ovh.equino.actracker.domain.tag.MetricsExistenceVerifier;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.*;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.*;
import static java.util.stream.Collectors.toUnmodifiableSet;

class Activity implements Entity {

    private final ActivityId id;
    private final User creator;
    private String title;
    private Instant startTime;
    private Instant endTime;
    private String comment;
    private final Set<TagId> tags;
    private final List<MetricValue> metricValues;
    private boolean deleted;

    private final TagsExistenceVerifier tagsExistenceVerifier;
    private final MetricsExistenceVerifier metricsExistenceVerifier;

    private Activity(
            ActivityId id,
            User creator,
            String title,
            Instant startTime,
            Instant endTime,
            String comment,
            Collection<TagId> tags,
            Collection<MetricValue> metricValues,
            boolean deleted,
            TagsExistenceVerifier tagsExistenceVerifier) {

        this.id = requireNonNull(id);
        this.creator = requireNonNull(creator);
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.comment = comment;
        this.tags = new HashSet<>(tags);
        this.metricValues = new ArrayList<>(metricValues);
        this.deleted = deleted;

        this.tagsExistenceVerifier = tagsExistenceVerifier;
        this.metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);
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
                activity.metricValues(),
                false,
                tagsExistenceVerifier
        );
        newActivity.validate();
        return newActivity;
    }

    private static List<TagId> toTagIds(ActivityDto activity) {
        return requireNonNullElse(activity.tags(), new HashSet<UUID>()).stream()
                .map(TagId::new)
                .toList();
    }

    void rename(String newTitle, User editor) {
        if (isNotAvailableFor(editor)) {
            throw new EntityEditForbidden(Activity.class);
        }
        this.title = newTitle;
    }

    void finish(Instant endTime, User updater) {
        if (isNotAvailableFor(updater)) {
            throw new EntityEditForbidden(Activity.class);
        }
        this.endTime = endTime;
        this.validate();
    }

    void start(Instant startTime, User updater) {
        if (isNotAvailableFor(updater)) {
            throw new EntityEditForbidden(Activity.class);
        }
        this.startTime = startTime;
        this.validate();
    }

    void updateComment(String comment, User updater) {
        if (isNotAvailableFor(updater)) {
            throw new EntityEditForbidden(Activity.class);
        }
        this.comment = comment;
    }

    void assignTag(TagId tagId, User updater) {
        if (isNotAvailableFor(updater)) {
            throw new EntityEditForbidden(Activity.class);
        }
        this.tags.add(tagId);
        validate();
    }

    void removeTag(TagId tagId, User updater) {
        if (isNotAvailableFor(updater)) {
            throw new EntityEditForbidden(Activity.class);
        }
        this.tags.remove(tagId);
    }

    void delete(User remover) {
        if (isNotAvailableFor(remover)) {
            throw new EntityEditForbidden(Activity.class);
        }
        this.deleted = true;
    }

    void updateTo(ActivityDto activity) {
        Set<TagId> deletedAssignedTags = tagsExistenceVerifier.notExisting(this.tags);
        Set<MetricId> deletedAssignedMetrics = metricsExistenceVerifier.notExisting(this.tags, this.selectedMetrics());
        List<MetricValue> deletedAssignedValues = valuesForMetricIds(deletedAssignedMetrics);
        this.title = activity.title();
        this.startTime = activity.startTime();
        this.endTime = activity.endTime();
        this.comment = activity.comment();
        this.tags.clear();
        this.tags.addAll(toTagIds(activity));
        this.metricValues.clear();
        this.metricValues.addAll(activity.metricValues());
        validate();
        this.tags.addAll(deletedAssignedTags);
        this.metricValues.addAll(deletedAssignedValues);
    }

    private List<MetricValue> valuesForMetricIds(Collection<MetricId> metricIds) {
        List<UUID> metricUUIDs = metricIds.stream()
                .map(MetricId::id)
                .toList();
        return this.metricValues.stream()
                .filter(metricValue -> metricUUIDs.contains(metricValue.metricId()))
                .toList();
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
                activity.metricValues(),
                activity.deleted(),
                tagsExistenceVerifier
        );
    }

    ActivityDto forStorage() {
        Set<UUID> tagIds = tags.stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());
        return new ActivityDto(
                id.id(),
                creator.id(),
                title,
                startTime,
                endTime,
                comment,
                tagIds,
                unmodifiableList(metricValues),
                deleted
        );
    }

    ActivityDto forClient() {
        Set<UUID> tagIds = tagsExistenceVerifier.existing(tags).stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());
        List<MetricValue> metricValues = valuesForMetricIds(
                metricsExistenceVerifier.existing(this.tags, this.selectedMetrics())
        );

        return new ActivityDto(
                id.id(),
                creator.id(),
                title,
                startTime,
                endTime,
                comment,
                tagIds,
                unmodifiableList(metricValues),
                deleted
        );
    }

    ActivityChangedNotification forChangeNotification() {
        Set<UUID> tagIds = tags.stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());
        ActivityDto dto = new ActivityDto(
                id.id(),
                creator.id(),
                title,
                startTime,
                endTime,
                comment,
                tagIds,
                unmodifiableList(metricValues),
                deleted
        );
        return new ActivityChangedNotification(dto);
    }

    boolean isAvailableFor(User user) {
        return creator.equals(user);
    }

    boolean isNotAvailableFor(User user) {
        return !isAvailableFor(user);
    }

    boolean isStarted() {
        return nonNull(this.startTime);
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

    String comment() {
        return comment;
    }

    Set<MetricId> selectedMetrics() {
        return metricValues.stream()
                .map(MetricValue::metricId)
                .map(MetricId::new)
                .collect(toUnmodifiableSet());
    }

    String title() {
        return this.title;
    }

    boolean deleted() {
        return this.deleted;
    }
}
