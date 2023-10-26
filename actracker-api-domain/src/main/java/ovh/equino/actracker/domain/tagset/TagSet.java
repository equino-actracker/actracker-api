package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.Entity;
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
    final Set<TagId> tags;
    private boolean deleted;

    private final TagsExistenceVerifier tagsExistenceVerifier;
    private final TagSetValidator validator;

    TagSet(TagSetId id,
           User creator,
           String name,
           Collection<TagId> tags,
           boolean deleted,
           TagSetValidator validator,
           TagsExistenceVerifier tagsExistenceVerifier) {

        this.id = requireNonNull(id);
        this.creator = requireNonNull(creator);
        this.name = name;
        this.tags = new HashSet<>(tags);
        this.deleted = deleted;

        this.validator = validator;
        this.tagsExistenceVerifier = tagsExistenceVerifier;
    }

    public static TagSet create(TagSetDto tagSet, User creator, TagsExistenceVerifier tagsExistenceVerifier) {
        TagSet newTagSet = new TagSet(
                new TagSetId(),
                creator,
                tagSet.name(),
                toTagIds(tagSet),
                false,
                new TagSetValidator(tagsExistenceVerifier),
                tagsExistenceVerifier
        );
        newTagSet.validate();
        return newTagSet;
    }

    public void rename(String newName, User updater) {
        new TagSetEditOperation(updater, this, tagsExistenceVerifier,
                () -> name = newName
        ).execute();
    }

    public void assignTag(TagId newTag, User updater) {
        new TagSetEditOperation(updater, this, tagsExistenceVerifier,
                () -> tags.add(newTag)
        ).execute();
    }

    public void removeTag(TagId tag, User updater) {
        new TagSetEditOperation(updater, this, tagsExistenceVerifier,
                () -> tags.remove(tag)
        ).execute();
    }

    public void delete(User remover) {
        new TagSetEditOperation(remover, this, tagsExistenceVerifier,
                () -> deleted = true
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
                new TagSetValidator(tagsExistenceVerifier),
                tagsExistenceVerifier
        );
    }

    public TagSetDto forStorage() {
        Set<UUID> tagIds = tags.stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());
        return new TagSetDto(id.id(), creator.id(), name, tagIds, deleted);
    }

    // TODO remove this and all unused methods
    public TagSetDto forClient(User client) {
        if (isNotAvailableFor(client)) {
            throw new EntityNotFoundException(TagSet.class, this.id.id());
        }
        Set<UUID> tagIds = tagsExistenceVerifier.existing(tags).stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());

        return new TagSetDto(id.id(), creator.id(), name, tagIds, deleted);
    }

    public TagSetChangedNotification forChangeNotification() {
        Set<UUID> tagIds = tags.stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());
        TagSetDto dto = new TagSetDto(id.id(), creator.id(), name, tagIds, deleted);
        return new TagSetChangedNotification(dto);
    }

    boolean isAvailableFor(User user) {
        return creator.equals(user);
    }

    boolean isNotAvailableFor(User user) {
        return !isAvailableFor(user);
    }

    @Override
    public void validate() {
        validator.validate(this);
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

    // TODO think about extracting it to superclass
    public TagSetId id() {
        return this.id;
    }
}
