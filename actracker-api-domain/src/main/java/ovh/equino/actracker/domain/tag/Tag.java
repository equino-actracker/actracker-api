package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.exception.EntityEditForbidden;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.UUID.randomUUID;
import static java.util.function.Predicate.isEqual;

public class Tag implements Entity {

    private final TagId id;
    private final User creator;
    private String name;
    final List<Metric> metrics;
    final List<Share> shares;
    private boolean deleted;

    private final TagValidator validator;

    Tag(TagId id,
        User creator,
        String name,
        Collection<Metric> metrics,
        List<Share> shares,
        boolean deleted,
        TagValidator validator) {

        this.id = requireNonNull(id);
        this.creator = requireNonNull(creator);
        this.name = name;
        this.metrics = new ArrayList<>(metrics);
        this.shares = new ArrayList<>(shares);
        this.deleted = deleted;

        this.validator = validator;
    }

    public static Tag create(TagDto tag, User creator) {

        List<Metric> metrics = requireNonNullElse(tag.metrics(), new ArrayList<MetricDto>()).stream()
                .map(metric -> Metric.create(metric, creator))
                .toList();

        Tag newTag = new Tag(
                new TagId(),
                creator,
                tag.name(),
                metrics,
                tag.shares(),
                false,
                new TagValidator()
        );

        newTag.validate();
        return newTag;
    }

    public void rename(String newName, User updater) {
        new TagEditOperation(updater, this, () ->
                this.name = newName
        ).execute();
    }

    public void addMetric(String name, MetricType type, User updater) {
        Metric newMetric = new Metric(new MetricId(randomUUID()), updater, name, type, false);
        new TagEditOperation(updater, this, () ->
                this.metrics.add(newMetric)
        ).execute();
    }

    public void deleteMetric(MetricId metricId, User updater) {
        new TagEditOperation(updater, this, () ->
                this.metrics.stream()
                        .filter(metric -> metric.id().equals(metricId))
                        .findFirst()
                        .ifPresent(Metric::delete)
        ).execute();
    }

    public void renameMetric(String newName, MetricId metricId, User updater) {
        new TagEditOperation(updater, this, () ->
                this.metrics.stream()
                        .filter(metric -> metric.id().equals(metricId))
                        .findFirst()
                        .ifPresent(metric -> metric.rename(newName))
        ).execute();
    }

    public void delete(User remover) {
        new TagEditOperation(remover, this, () -> {
            this.metrics.forEach(Metric::delete);
            this.deleted = true;
        }).execute();
    }

    public void share(Share newShare, User granter) {
        new TagEditOperation(granter, this, () -> {

            List<String> existingGranteeNames = this.shares.stream()
                    .map(Share::granteeName)
                    .toList();
            if (!existingGranteeNames.contains(newShare.granteeName())) {
                this.shares.add(newShare);
            }

        }).execute();
    }

    public void unshare(String granteeName, User granter) {
        new TagEditOperation(granter, this, () -> {

            List<Share> sharesWithExclusion = this.shares.stream()
                    .filter(share -> !share.granteeName().equals(granteeName))
                    .toList();
            this.shares.clear();
            this.shares.addAll(sharesWithExclusion);

        }).execute();
    }

    void updateTo(TagDto tag, User updater) {
        if (isEditForbiddenFor(updater)) {
            throw new EntityEditForbidden(Tag.class);
        }

        Collection<Metric> newMetrics = findNewMetrics(tag.metrics());
        Collection<Metric> deletedMetrics = findDeletedMetrics(tag.metrics());
        Collection<Metric> updatedMetrics = findUpdatedMetrics(tag.metrics());

        this.name = tag.name();
        this.metrics.clear();
        this.metrics.addAll(newMetrics);
        this.metrics.addAll(deletedMetrics);
        this.metrics.addAll(updatedMetrics);
        validate();
    }

    public static Tag fromStorage(TagDto tag) {
        List<Metric> metrics = tag.metrics().stream()
                .map(Metric::fromStorage)
                .toList();
        return new Tag(
                new TagId(tag.id()),
                new User(tag.creatorId()),
                tag.name(),
                metrics,
                tag.shares(),
                tag.deleted(),
                new TagValidator()
        );
    }

    public TagDto forStorage() {
        List<MetricDto> metrics = this.metrics.stream()
                .map(Metric::forStorage)
                .toList();
        return new TagDto(id.id(), creator.id(), name, metrics, shares, deleted);
    }

    public TagDto forClient(User client) {
        if (isNotAccessibleFor(client)) {
            throw new EntityNotFoundException(Tag.class, this.id.id());
        }
        List<MetricDto> metrics = this.metrics.stream()
                .filter(Metric::isNotDeleted)
                .map(Metric::forStorage)
                .toList();
        return new TagDto(id.id(), creator.id(), name, metrics, shares, deleted);
    }

    public TagChangedNotification forChangeNotification() {
        List<MetricDto> metrics = this.metrics.stream()
                .map(Metric::forStorage)
                .toList();
        TagDto dto = new TagDto(id.id(), creator.id(), name, metrics, shares, deleted);
        return new TagChangedNotification(dto);
    }

    boolean isDeleted() {
        return deleted;
    }

    boolean isNotDeleted() {
        return !isDeleted();
    }

    boolean isAccessibleFor(User user) {
        return creator.equals(user) || isGrantee(user);
    }

    private boolean isGrantee(User user) {
        return shares.stream()
                .map(Share::grantee)
                .filter(Objects::nonNull)
                .anyMatch(isEqual(user));
    }

    boolean isNotAccessibleFor(User user) {
        return !isAccessibleFor(user);
    }

    private boolean isEditForbiddenFor(User user) {
        return !creator.equals(user);
    }

    @Override
    public void validate() {
        validator.validate(this);
    }

    String name() {
        return name;
    }

    private Collection<Metric> findUpdatedMetrics(Collection<MetricDto> requestedMetrics) {
        return requireNonNullElse(requestedMetrics, new ArrayList<MetricDto>())
                .stream()
                .map(this::toUpdatedMetric)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<Metric> toUpdatedMetric(MetricDto metricDto) {
        Optional<Metric> metricForUpdate = this.metrics.stream()
                .filter(Metric::isNotDeleted)
                .filter(metric -> metric.id().id().equals(metricDto.id()))
                .findAny();
        metricForUpdate.ifPresent(metric -> metric.updateTo(metricDto));
        return metricForUpdate;
    }

    private Collection<Metric> findDeletedMetrics(Collection<MetricDto> requestedMetrics) {
        List<UUID> requestedMetricIds = requireNonNullElse(requestedMetrics, new ArrayList<MetricDto>())
                .stream()
                .map(MetricDto::id)
                .filter(Objects::nonNull)
                .toList();
        List<Metric> deletedMetrics = this.metrics.stream()
                .filter(metric -> metric.isDeleted() || !requestedMetricIds.contains(metric.id().id()))
                .toList();
        deletedMetrics.forEach(Metric::delete);

        return deletedMetrics;
    }

    private Collection<Metric> findNewMetrics(Collection<MetricDto> requestedMetrics) {
        List<UUID> existingMetricIds = this.metrics.stream()
                .filter(Metric::isNotDeleted)
                .map(Metric::id)
                .map(MetricId::id)
                .toList();
        return requireNonNullElse(requestedMetrics, new ArrayList<MetricDto>())
                .stream()
                .filter(metric -> !existingMetricIds.contains(metric.id()))
                .map(metric -> Metric.create(metric, this.creator))
                .toList();
    }

    @Override
    public User creator() {
        return creator;
    }

    // TODO think about extracting it to superclass
    public TagId id() {
        return this.id;
    }

}
