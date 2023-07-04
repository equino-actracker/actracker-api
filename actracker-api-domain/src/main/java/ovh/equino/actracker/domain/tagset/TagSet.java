package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.exception.EntityEditForbidden;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.user.User;

import java.util.*;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;

public class TagSet implements Entity {

    private final TagSetId id;
    private final User creator;
    private String name;
    private final Set<TagId> tags;
    private boolean deleted;

    private final TagsExistenceVerifier tagsExistenceVerifier;

    private TagSet(
            TagSetId id,
            User creator,
            String name,
            Collection<TagId> tags,
            boolean deleted,
            TagsExistenceVerifier tagsExistenceVerifier) {

        this.id = requireNonNull(id);
        this.creator = requireNonNull(creator);
        this.name = name;
        this.tags = new HashSet<>(tags);
        this.deleted = deleted;
        this.tagsExistenceVerifier = tagsExistenceVerifier;
    }

    static TagSet create(TagSetDto tagSet, User creator, TagsExistenceVerifier tagsExistenceVerifier) {
        TagSet newTagSet = new TagSet(
                new TagSetId(),
                creator,
                tagSet.name(),
                toTagIds(tagSet),
                false,
                tagsExistenceVerifier
        );
        newTagSet.validate();
        return newTagSet;
    }

    public void rename(String newName, User updater) {
        new TagSetEditOperation(updater, this,
                () -> this.name = newName
        ).execute();
    }

    public void assignTag(TagId newTag, User updater) {
        if (isNotAvailableFor(updater)) {
            throw new EntityEditForbidden(TagSet.class);
        }
        this.tags.add(newTag);
        validate();
    }

    public void removeTag(TagId tag, User updater) {
        if (isNotAvailableFor(updater)) {
            throw new EntityEditForbidden(TagSet.class);
        }
        this.tags.remove(tag);
    }

    public void delete(User remover) {
        new TagSetEditOperation(remover, this,
                () -> this.deleted = true
        ).execute();
    }

    void updateTo(TagSetDto tagSet) {
        Set<TagId> deletedAssignedTags = tagsExistenceVerifier.notExisting(this.tags);
        this.name = tagSet.name();
        this.tags.clear();
        this.tags.addAll(toTagIds(tagSet));
        this.validate();
        this.tags.addAll(deletedAssignedTags);
    }

    public static TagSet fromStorage(TagSetDto tagSet, TagsExistenceVerifier tagsExistenceVerifier) {
        return new TagSet(
                new TagSetId(tagSet.id()),
                new User(tagSet.creatorId()),
                tagSet.name(),
                toTagIds(tagSet),
                tagSet.deleted(),
                tagsExistenceVerifier
        );
    }

    public TagSetDto forStorage() {
        Set<UUID> tagIds = tags.stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());
        return new TagSetDto(id.id(), creator.id(), name, tagIds, deleted);
    }

    public TagSetDto forClient(User client) {
        if (isNotAvailableFor(client)) {
            throw new EntityNotFoundException(TagSet.class, this.id.id());
        }
        Set<UUID> tagIds = tagsExistenceVerifier.existing(tags).stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());

        return new TagSetDto(id.id(), creator.id(), name, tagIds, deleted);
    }

    boolean isAvailableFor(User user) {
        return creator.equals(user);
    }

    boolean isNotAvailableFor(User user) {
        return !isAvailableFor(user);
    }

    @Override
    public void validate() {
        new TagSetValidator(this, tagsExistenceVerifier).validate();
    }

    String name() {
        return this.name;
    }

    Set<TagId> tags() {
        return unmodifiableSet(tags);
    }

    boolean deleted() {
        return this.deleted;
    }

    private static List<TagId> toTagIds(TagSetDto tagSet) {
        return requireNonNullElse(tagSet.tags(), new HashSet<UUID>()).stream()
                .map(TagId::new)
                .toList();
    }

    @Override
    public User creator() {
        return creator;
    }
}
