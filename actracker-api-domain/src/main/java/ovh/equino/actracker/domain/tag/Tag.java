package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.user.User;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

class Tag implements Entity {

    private final TagId id;
    private final User creator;
    private String name;
    private final List<Metric> metrics;
    private boolean deleted;

    private Tag(
            TagId id,
            User creator,
            String name,
            Collection<Metric> metrics,
            boolean deleted) {

        this.id = requireNonNull(id);
        this.creator = requireNonNull(creator);
        this.name = name;
        this.deleted = deleted;
        this.metrics = new ArrayList<>(metrics);
    }

    static Tag create(TagDto tag, User creator) {

        List<Metric> metrics = requireNonNullElse(tag.metrics(), new ArrayList<MetricDto>()).stream()
                .map(metric -> Metric.create(metric, creator))
                .toList();

        Tag newTag = new Tag(
                new TagId(),
                creator,
                tag.name(),
                metrics,
                false
        );

        newTag.validate();
        return newTag;
    }

    void updateTo(TagDto tag) {

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

    void delete() {
        this.metrics.forEach(Metric::delete);
        this.deleted = true;
    }

    static Tag fromStorage(TagDto tag) {
        List<Metric> metrics = tag.metrics().stream()
                .map(Metric::fromStorage)
                .toList();
        return new Tag(
                new TagId(tag.id()),
                new User(tag.creatorId()),
                tag.name(),
                metrics,
                tag.deleted()
        );
    }

    TagDto forStorage() {
        List<MetricDto> metrics = this.metrics.stream()
                .map(Metric::forStorage)
                .toList();
        return new TagDto(id.id(), creator.id(), name, metrics, deleted);
    }

    TagDto forClient() {
        List<MetricDto> metrics = this.metrics.stream()
                .filter(Metric::isNotDeleted)
                .map(Metric::forStorage)
                .toList();
        return new TagDto(id.id(), creator.id(), name, metrics, deleted);
    }

    TagChangedNotification forChangeNotification() {
        List<MetricDto> metrics = this.metrics.stream()
                .filter(Metric::isNotDeleted)
                .map(Metric::forStorage)
                .toList();
        TagDto dto = new TagDto(id.id(), creator.id(), name, metrics, deleted);
        return new TagChangedNotification(dto);
    }

    TagId id() {
        return id;
    }

    boolean isDeleted() {
        return deleted;
    }

    boolean isNotDeleted() {
        return !isDeleted();
    }

    boolean isAvailableFor(User user) {
        return creator.equals(user);
    }

    boolean isNotAvailableFor(User user) {
        return !isAvailableFor(user);
    }

    @Override
    public void validate() {
        new TagValidator(this).validate();
    }

    String name() {
        return name;
    }

    private Collection<Metric> findUpdatedMetrics(Collection<MetricDto> requestedMetrics) {
//        List<UUID> existingMetricIds = this.metrics.stream()
//                .filter(Metric::isNotDeleted)
//                .map(Metric::id)
//                .map(MetricId::id)
//                .toList();
        return requireNonNullElse(requestedMetrics, new ArrayList<MetricDto>())
                .stream()
//                .filter(metric -> existingMetricIds.contains(metric.id()))
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
}
