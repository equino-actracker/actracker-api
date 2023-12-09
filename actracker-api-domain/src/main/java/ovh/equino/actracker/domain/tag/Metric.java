package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.user.User;

import static java.util.Objects.requireNonNull;

public class Metric implements Entity {

    private final MetricId id;
    private final User creator;
    private String name;
    private final MetricType type;
    private boolean deleted;

    Metric(MetricId id, User creator, String name, MetricType type, boolean deleted) {
        this.id = requireNonNull(id);
        this.creator = requireNonNull(creator);
        this.name = name;
        this.type = requireNonNull(type);
        this.deleted = deleted;
    }

    // TODO delete
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

    void rename(String newName) {
        this.name = newName;
    }

    void delete() {
        this.deleted = true;
    }

    // TODO delete
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

    boolean deleted() {
        return deleted;
    }

    boolean isNotDeleted() {
        return !deleted();
    }

    MetricId id() {
        return id;
    }

    MetricType type() {
        return type;
    }

    String name() {
        return this.name;
    }

    @Override
    public User creator() {
        return creator;
    }
}
