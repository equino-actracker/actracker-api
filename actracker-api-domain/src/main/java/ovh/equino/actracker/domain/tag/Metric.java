package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.user.User;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

class Metric implements Entity {

    private final MetricId id;
    private final User creator;
    private String name;
    private final MetricType type;
    private boolean deleted;

    private Metric(MetricId id, User creator, String name, MetricType type, boolean deleted) {
        this.id = requireNonNull(id);
        this.creator = requireNonNull(creator);
        this.name = name;
        this.type = requireNonNull(type);
        this.deleted = deleted;
    }

    static Metric create(MetricDto metric, User creator) {
        Metric newMetric = new Metric(
                new MetricId(),
                creator,
                metric.name(),
                metric.type(),
                metric.deleted()
        );
        newMetric.validate();
        return newMetric;
    }

    void updateTo(MetricDto metric) {
        this.name = metric.name();
        if (metric.type() != this.type) {
            throw new EntityInvalidException(Metric.class, singletonList("Metric type cannot be changed."));
        }
        validate();
    }

    void delete() {
        this.deleted = true;
    }

    static Metric fromStorage(MetricDto metric) {
        return new Metric(
                new MetricId(metric.id()),
                new User(metric.creatorId()),
                metric.name(),
                metric.type(),
                metric.deleted()
        );
    }

    MetricDto forStorage() {
        return new MetricDto(
                id.id(),
                creator.id(),
                name,
                type,
                deleted
        );
    }

    MetricDto forClient() {
        return new MetricDto(
                id.id(),
                creator.id(),
                name,
                type,
                deleted
        );
    }

    boolean isDeleted() {
        return deleted;
    }

    boolean isNotDeleted() {
        return !isDeleted();
    }

    MetricId id() {
        return id;
    }

    MetricType type() {
        return type;
    }
}
