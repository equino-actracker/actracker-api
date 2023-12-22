package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.user.User;

import static java.util.Objects.requireNonNull;

public final class Metric implements Entity {

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

    void rename(String newName) {
        this.name = newName;
    }

    void delete() {
        this.deleted = true;
    }

    // TODO remove
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
