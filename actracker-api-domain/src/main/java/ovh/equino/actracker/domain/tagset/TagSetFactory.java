package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.tag.TagDataSource;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.User;

import java.util.ArrayList;
import java.util.Collection;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.requireNonNullElse;

public final class TagSetFactory {

    private static final boolean DELETED = TRUE;

    private final TagSetDataSource tagSetDataSource;
    private final TagDataSource tagDataSource;

    TagSetFactory(TagSetDataSource tagSetDataSource, TagDataSource tagDataSource) {
        this.tagSetDataSource = tagSetDataSource;
        this.tagDataSource = tagDataSource;
    }

    public TagSet create(User creator,
                         String name,
                         Collection<TagId> tags) {

        // TODO verifiers should be injected
        var tagSetAccessibilityVerifier = new TagSetsAccessibilityVerifier(tagSetDataSource);
        var tagsAccessibilityVerifier = new TagsAccessibilityVerifier(tagDataSource);
        var validator = new TagSetValidator();

        var nonNullTags = requireNonNullElse(tags, new ArrayList<TagId>());
        validateTagsAccessible(creator, nonNullTags, tagsAccessibilityVerifier);

        var tagSet = new TagSet(
                new TagSetId(),
                creator,
                name,
                nonNullTags,
                !DELETED,
                validator,
                tagSetAccessibilityVerifier,
                tagsAccessibilityVerifier
        );
        tagSet.validate();
        return tagSet;
    }

    public TagSet reconstitute(User actor,  // TODO remove
                               TagSetId id,
                               User creator,
                               String name,
                               Collection<TagId> tags,
                               boolean deleted) {

        // TODO verifiers should be injected
        var tagSetAccessibilityVerifier = new TagSetsAccessibilityVerifier(tagSetDataSource);
        var tagsAccessibilityVerifier = new TagsAccessibilityVerifier(tagDataSource);
        var validator = new TagSetValidator();

        return new TagSet(
                id,
                creator,
                name,
                tags,
                deleted,
                validator,
                tagSetAccessibilityVerifier,
                tagsAccessibilityVerifier
        );
    }

    // TODO when verifier will be an injected field, parameter not required
    private void validateTagsAccessible(User user, Collection<TagId> tags, TagsAccessibilityVerifier tagsAccessibilityVerifier) {
        tagsAccessibilityVerifier.nonAccessibleFor(user, tags)
                .stream()
                .findFirst()
                .ifPresent((inaccessibleTag) -> {
                    String errorMessage = "Tag with ID %s not found".formatted(inaccessibleTag.id());
                    throw new EntityInvalidException(TagSet.class, errorMessage);
                });
    }
}
