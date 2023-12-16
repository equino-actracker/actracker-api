package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.exception.EntityEditForbidden;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;

public final class TagSet implements Entity {

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

    public void rename(String newName, User updater) {
        if (!creator.equals(updater) && !tagSetsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(TagSet.class, id.id());
        }
        if (!this.isEditableFor(updater)) {
            throw new EntityEditForbidden(TagSet.class);
        }
        name = newName;
        this.validate();
    }

    public void assignTag(TagId newTag, User updater) {
        if (!creator.equals(updater) && !tagSetsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(TagSet.class, id.id());
        }
        if (!this.isEditableFor(updater)) {
            throw new EntityEditForbidden(TagSet.class);
        }
        if (!tagsAccessibilityVerifier.isAccessible(newTag)) {
            String errorMessage = "Tag with ID %s does not exist".formatted(newTag.id());
            throw new EntityInvalidException(TagSet.class, errorMessage);
        }
        tags.add(newTag);
        this.validate();
    }

    public void removeTag(TagId tag, User updater) {
        if (!creator.equals(updater) && !tagSetsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(TagSet.class, id.id());
        }
        if (!this.isEditableFor(updater)) {
            throw new EntityEditForbidden(TagSet.class);
        }
        if (!tagsAccessibilityVerifier.isAccessible(tag)) {
            return;
        }
        tags.remove(tag);
        this.validate();
    }

    public void delete(User remover) {
        if (!creator.equals(remover) && !tagSetsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(TagSet.class, id.id());
        }
        if (!this.isEditableFor(remover)) {
            throw new EntityEditForbidden(TagSet.class);
        }
        this.deleted = true;
        this.validate();
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

    @Override
    public User creator() {
        return creator;
    }

    // TODO think about extracting it to superclass
    public TagSetId id() {
        return this.id;
    }
}
