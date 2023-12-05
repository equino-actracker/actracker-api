package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
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

    private final TagSetsAccessibilityVerifier tagSetsAccessibilityVerifier;
    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;
    private final TagSetValidator validator;

    TagSet(TagSetId id,
           User creator,
           String name,
           Collection<TagId> tags,
           boolean deleted,
           TagSetValidator validator,
           TagSetsAccessibilityVerifier tagSetsAccessibilityVerifier,
           TagsAccessibilityVerifier tagsAccessibilityVerifier) {

        this.id = requireNonNull(id);
        this.creator = requireNonNull(creator);
        this.name = name;
        this.tags = new HashSet<>(tags);
        this.deleted = deleted;

        this.tagSetsAccessibilityVerifier = tagSetsAccessibilityVerifier;
        this.tagsAccessibilityVerifier = tagsAccessibilityVerifier;
        this.validator = validator;
    }

    public static TagSet create(TagSetDto tagSet,
                                User creator,
                                TagSetsAccessibilityVerifier tagSetsAccessibilityVerifier,
                                TagsAccessibilityVerifier tagsAccessibilityVerifier) {

        TagSet newTagSet = new TagSet(
                new TagSetId(),
                creator,
                tagSet.name(),
                toTagIds(tagSet),
                false,
                new TagSetValidator(tagsAccessibilityVerifier),
                tagSetsAccessibilityVerifier,
                tagsAccessibilityVerifier
        );
        newTagSet.validate();
        return newTagSet;
    }

    public void rename(String newName, User updater) {
        if (!creator.equals(updater) && !tagSetsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(TagSet.class, id.id());
        }
        new TagSetEditOperation(updater, this, tagsAccessibilityVerifier,
                () -> name = newName
        ).execute();
    }

    public void assignTag(TagId newTag, User updater) {
        if (!creator.equals(updater) && !tagSetsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(TagSet.class, id.id());
        }
        new TagSetEditOperation(updater, this, tagsAccessibilityVerifier,
                () -> tags.add(newTag)
        ).execute();
    }

    public void removeTag(TagId tag, User updater) {
        if (!creator.equals(updater) && !tagSetsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(TagSet.class, id.id());
        }
        new TagSetEditOperation(updater, this, tagsAccessibilityVerifier,
                () -> tags.remove(tag)
        ).execute();
    }

    public void delete(User remover) {
        if (!creator.equals(remover) && !tagSetsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(TagSet.class, id.id());
        }
        new TagSetEditOperation(remover, this, tagsAccessibilityVerifier,
                () -> deleted = true
        ).execute();
    }

    public static TagSet fromStorage(TagSetDto tagSet,
                                     TagSetsAccessibilityVerifier tagSetsAccessibilityVerifier,
                                     TagsAccessibilityVerifier tagsAccessibilityVerifier) {

        return new TagSet(
                new TagSetId(tagSet.id()),
                new User(tagSet.creatorId()),
                tagSet.name(),
                toTagIds(tagSet),
                tagSet.deleted(),
                new TagSetValidator(tagsAccessibilityVerifier),
                tagSetsAccessibilityVerifier,
                tagsAccessibilityVerifier
        );
    }

    public TagSetDto forStorage() {
        Set<UUID> tagIds = tags.stream()
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
        return requireNonNullElse(tagSet.tags(), new HashSet<UUID>())
                .stream()
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
