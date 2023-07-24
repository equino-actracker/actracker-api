package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.*;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.*;
import static java.util.stream.Collectors.toUnmodifiableSet;

public class Activity implements Entity {

    private final ActivityId id;
    private final User creator;
    private String title;
    private Instant startTime;
    private Instant endTime;
    private String comment;
    final Set<TagId> tags;
    final List<MetricValue> metricValues;
    private boolean deleted;

    private final TagsExistenceVerifier tagsExistenceVerifier;
    private final MetricsExistenceVerifier metricsExistenceVerifier;
    private final ActivityValidator validator;

    Activity(
            ActivityId id,
            User creator,
            String title,
            Instant startTime,
            Instant endTime,
            String comment,
            Collection<TagId> tags,
            Collection<MetricValue> metricValues,
            boolean deleted,
            TagsExistenceVerifier tagsExistenceVerifier,
            MetricsExistenceVerifier metricsExistenceVerifier,
            ActivityValidator validator) {

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
        this.metricsExistenceVerifier = metricsExistenceVerifier;
        this.validator = validator;
    }

    public static Activity create(ActivityDto activity, User creator, TagsExistenceVerifier tagsExistenceVerifier, MetricsExistenceVerifier metricsExistenceVerifier) {
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
                tagsExistenceVerifier,
                metricsExistenceVerifier,
                new ActivityValidator(tagsExistenceVerifier, metricsExistenceVerifier)
        );
        newActivity.validate();
        return newActivity;
    }

    private static List<TagId> toTagIds(ActivityDto activity) {
        return requireNonNullElse(activity.tags(), new HashSet<UUID>()).stream()
                .map(TagId::new)
                .toList();
    }

    public void rename(String newTitle, User editor) {
        new ActivityEditOperation(editor, this, tagsExistenceVerifier, metricsExistenceVerifier,
                () -> this.title = newTitle
        ).execute();
    }

    public void start(Instant startTime, User updater) {
        new ActivityEditOperation(updater, this, tagsExistenceVerifier, metricsExistenceVerifier,
                () -> this.startTime = startTime
        ).execute();
    }

    public void finish(Instant endTime, User updater) {
        new ActivityEditOperation(updater, this, tagsExistenceVerifier, metricsExistenceVerifier,
                () -> this.endTime = endTime
        ).execute();
    }

    public void updateComment(String comment, User updater) {
        new ActivityEditOperation(updater, this, tagsExistenceVerifier, metricsExistenceVerifier,
                () -> this.comment = comment
        ).execute();
    }

    public void assignTag(TagId tagId, User updater) {
        new ActivityEditOperation(updater, this, tagsExistenceVerifier, metricsExistenceVerifier,
                () -> this.tags.add(tagId)
        ).execute();
    }

    public void removeTag(TagId tagId, User updater) {
        new ActivityEditOperation(updater, this, tagsExistenceVerifier, metricsExistenceVerifier,
                () -> this.tags.remove(tagId)
        ).execute();
    }

    public void setMetricValue(MetricValue newMetricValue, User updater) {
        new ActivityEditOperation(updater, this, tagsExistenceVerifier, metricsExistenceVerifier, () -> {

            List<MetricValue> otherValues = metricValues.stream()
                    .filter(value -> !value.metricId().equals(newMetricValue.metricId()))
                    .toList();
            metricValues.clear();
            metricValues.addAll(otherValues);
            metricValues.add(newMetricValue);

        }).execute();
    }

    public void unsetMetricValue(MetricId metricId, User updater) {
        new ActivityEditOperation(updater, this, tagsExistenceVerifier, metricsExistenceVerifier, () -> {

            List<MetricValue> remainingMetricValues = metricValues.stream()
                    .filter(value -> !value.metricId().equals(metricId.id()))
                    .toList();
            metricValues.clear();
            metricValues.addAll(remainingMetricValues);

        }).execute();
    }

    public void delete(User remover) {
        new ActivityEditOperation(remover, this, tagsExistenceVerifier, metricsExistenceVerifier,
                () -> this.deleted = true
        ).execute();
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

    public static Activity fromStorage(ActivityDto activity, TagsExistenceVerifier tagsExistenceVerifier, MetricsExistenceVerifier metricsExistenceVerifier) {
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
                tagsExistenceVerifier,
                metricsExistenceVerifier,
                new ActivityValidator(tagsExistenceVerifier, metricsExistenceVerifier)
        );
    }

    public ActivityDto forStorage() {
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

    public ActivityDto forClient(User client) {
        if (isNotAvailableFor(client)) {
            throw new EntityNotFoundException(Activity.class, this.id.id());
        }
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
        return creator.equals(user) || isSharedWith(user);
    }

    boolean isSharedWith(User user) {
        SharedTagsExistenceVerifier sharedTagsExistenceVerifier =
                new SharedTagsExistenceVerifier(tagsExistenceVerifier, user);
        return sharedTagsExistenceVerifier.containsSharedTags(this.tags);
    }

    boolean isNotAvailableFor(User user) {
        return !isAvailableFor(user);
    }

    public boolean isStarted() {
        return nonNull(this.startTime);
    }

    @Override
    public void validate() {
        validator.validate(this);
    }

    public Instant startTime() {
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

    @Override
    public User creator() {
        return creator;
    }
}
