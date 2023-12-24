package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.exception.EntityEditForbidden;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.MetricId;
import ovh.equino.actracker.domain.tag.MetricsAccessibilityVerifier;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.*;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;

public final class Activity implements Entity {

    private final ActivityId id;
    private final User creator;
    private String title;
    private Instant startTime;
    private Instant endTime;
    private String comment;
    private final Set<TagId> tags;
    private final List<MetricValue> metricValues;
    private boolean deleted;

    private final ActorExtractor actorExtractor;
    private final ActivitiesAccessibilityVerifier activitiesAccessibilityVerifier;
    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;
    private final MetricsAccessibilityVerifier metricsAccessibilityVerifier;
    private final ActivityValidator validator;

    Activity(ActivityId id,
             User creator,
             String title,
             Instant startTime,
             Instant endTime,
             String comment,
             Collection<TagId> tags,
             Collection<MetricValue> metricValues,
             boolean deleted,
             ActorExtractor actorExtractor,
             ActivitiesAccessibilityVerifier activitiesAccessibilityVerifier,
             TagsAccessibilityVerifier tagsAccessibilityVerifier,
             MetricsAccessibilityVerifier metricsAccessibilityVerifier,
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

        this.actorExtractor = actorExtractor;
        this.activitiesAccessibilityVerifier = activitiesAccessibilityVerifier;
        this.tagsAccessibilityVerifier = tagsAccessibilityVerifier;
        this.metricsAccessibilityVerifier = metricsAccessibilityVerifier;
        this.validator = validator;
    }

    public void rename(String newTitle, User editor) {
        if (!creator.equals(editor) && !activitiesAccessibilityVerifier.isAccessibleFor(editor, this.id)) {
            throw new EntityNotFoundException(Activity.class, id.id());
        }
        if (!this.isEditableFor(editor)) {
            throw new EntityEditForbidden(Activity.class);
        }
        this.title = newTitle;
        this.validate();
    }

    public void start(Instant startTime, User updater) {
        if (!creator.equals(updater) && !activitiesAccessibilityVerifier.isAccessibleFor(updater, this.id)) {
            throw new EntityNotFoundException(Activity.class, id.id());
        }
        if (!this.isEditableFor(updater)) {
            throw new EntityEditForbidden(Activity.class);
        }
        this.startTime = startTime;
        this.validate();
    }

    public void finish(Instant endTime, User updater) {
        if (!creator.equals(updater) && !activitiesAccessibilityVerifier.isAccessibleFor(updater, this.id)) {
            throw new EntityNotFoundException(Activity.class, id.id());
        }
        if (!this.isEditableFor(updater)) {
            throw new EntityEditForbidden(Activity.class);
        }
        this.endTime = endTime;
        this.validate();
    }

    public void updateComment(String comment, User updater) {
        if (!creator.equals(updater) && !activitiesAccessibilityVerifier.isAccessibleFor(updater, this.id)) {
            throw new EntityNotFoundException(Activity.class, id.id());
        }
        if (!this.isEditableFor(updater)) {
            throw new EntityEditForbidden(Activity.class);
        }
        this.comment = comment;
        this.validate();
    }

    public void assignTag(TagId tagId, User updater) {
        if (!creator.equals(updater) && !activitiesAccessibilityVerifier.isAccessibleFor(updater, this.id)) {
            throw new EntityNotFoundException(Activity.class, id.id());
        }
        if (!this.isEditableFor(updater)) {
            throw new EntityEditForbidden(Activity.class);
        }
        if (!tagsAccessibilityVerifier.isAccessibleFor(updater, tagId)) {
            String errorMessage = "Tag with ID %s does not exist".formatted(tagId.id());
            throw new EntityInvalidException(Activity.class, errorMessage);
        }
        this.tags.add(tagId);
        this.validate();
    }

    public void removeTag(TagId tagId, User updater) {
        if (!creator.equals(updater) && !activitiesAccessibilityVerifier.isAccessibleFor(updater, this.id)) {
            throw new EntityNotFoundException(Activity.class, id.id());
        }
        if (!this.isEditableFor(updater)) {
            throw new EntityEditForbidden(Activity.class);
        }
        if (!tagsAccessibilityVerifier.isAccessibleFor(updater, tagId)) {
            return;
        }
        this.tags.remove(tagId);
        this.validate();
    }

    public void setMetricValue(MetricValue newMetricValue, User updater) {
        MetricId metricId = new MetricId(newMetricValue.metricId());
        if (!creator.equals(updater) && !activitiesAccessibilityVerifier.isAccessibleFor(updater, this.id)) {
            throw new EntityNotFoundException(Activity.class, id.id());
        }
        if (!this.isEditableFor(updater)) {
            throw new EntityEditForbidden(Activity.class);
        }
        if (!metricsAccessibilityVerifier.isAccessibleFor(updater, metricId, tags)) {
            String errorMessage = "Metric with ID %s does not exist in selected tags".formatted(metricId.id());
            throw new EntityInvalidException(Activity.class, errorMessage);
        }
        List<MetricValue> otherValues = metricValues.stream()
                .filter(value -> !value.metricId().equals(newMetricValue.metricId()))
                .toList();
        metricValues.clear();
        metricValues.addAll(otherValues);
        metricValues.add(newMetricValue);
        this.validate();
    }

    public void unsetMetricValue(MetricId metricId, User updater) {
        if (!creator.equals(updater) && !activitiesAccessibilityVerifier.isAccessibleFor(updater, this.id)) {
            throw new EntityNotFoundException(Activity.class, id.id());
        }
        if (!this.isEditableFor(updater)) {
            throw new EntityEditForbidden(Activity.class);
        }
        if (!metricsAccessibilityVerifier.isAccessibleFor(updater, metricId, tags)) {
            return;
        }
        List<MetricValue> remainingMetricValues = metricValues.stream()
                .filter(value -> !value.metricId().equals(metricId.id()))
                .toList();
        metricValues.clear();
        metricValues.addAll(remainingMetricValues);
        this.validate();
    }

    public void delete(User remover) {
        if (!creator.equals(remover) && !activitiesAccessibilityVerifier.isAccessibleFor(remover, this.id)) {
            throw new EntityNotFoundException(Activity.class, id.id());
        }
        if (!this.isEditableFor(remover)) {
            throw new EntityEditForbidden(Activity.class);
        }
        this.deleted = true;
        this.validate();
    }

    // TODO remove
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

    // TODO change
    public ActivityChangedNotification forChangeNotification() {
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

    List<MetricValue> metricValues() {
        return unmodifiableList(metricValues);
    }

    String comment() {
        return comment;
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

    // TODO think about extracting it to superclass
    public ActivityId id() {
        return this.id;
    }
}
