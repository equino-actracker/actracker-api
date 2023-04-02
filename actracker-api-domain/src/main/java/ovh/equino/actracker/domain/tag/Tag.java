package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.user.User;

import static java.util.Objects.requireNonNull;

class Tag implements Entity {

    private final TagId id;
    private final User creator;
    private String name;
    private boolean deleted;

    Tag(TagId newId, TagDto tagData, User creator) {
        this.id = requireNonNull(newId);
        this.creator = requireNonNull(creator);
        this.name = tagData.name();
        this.deleted = false;
        validate();
    }

    static Tag fromDto(TagDto tagData) {
        Tag tag = new Tag(
                new TagId(tagData.id()),
                tagData,
                new User(tagData.creatorId())
        );
        tag.deleted = tagData.deleted();
        return tag;
    }

    TagDto toDto() {
        return new TagDto(id.id(), creator.id(), name, deleted);
    }

    void updateTo(TagDto tag) {
        this.name = tag.name();
        validate();
    }

    void delete() {
        this.deleted = true;
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
