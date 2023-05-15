package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.metric.Metric;
import ovh.equino.actracker.domain.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

class Tag implements Entity {

    private final TagId id;
    private final User creator;
    private String name;
    private boolean deleted;
    private final List<Metric> metrics;

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
        Tag newTag = new Tag(
                new TagId(),
                creator,
                tag.name(),
                tag.metrics(),
                false
        );
        newTag.validate();
        return newTag;
    }

    void updateTo(TagDto tag) {
        this.name = tag.name();
        this.metrics.clear();
        this.metrics.addAll(tag.metrics());
        validate();
    }

    void delete() {
        this.deleted = true;
    }

    static Tag fromStorage(TagDto tag) {
        return new Tag(
                new TagId(tag.id()),
                new User(tag.creatorId()),
                tag.name(),
                tag.metrics(),
                tag.deleted()
        );
    }

    TagDto forStorage() {
        return new TagDto(id.id(), creator.id(), name, unmodifiableList(metrics), deleted);
    }

    TagDto forClient() {
        return new TagDto(id.id(), creator.id(), name, unmodifiableList(metrics), deleted);
    }

    TagChangedNotification forChangeNotification() {
        TagDto dto = new TagDto(id.id(), creator.id(), name, unmodifiableList(metrics), deleted);
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
}
