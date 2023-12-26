package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.util.ArrayList;
import java.util.Collection;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.requireNonNullElse;

public final class TagSetFactory {

    private static final boolean DELETED = TRUE;

    private final ActorExtractor actorExtractor;
    private final TagSetsAccessibilityVerifier tagSetsAccessibilityVerifier;
    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;

    TagSetFactory(ActorExtractor actorExtractor,
                  TagSetsAccessibilityVerifier tagSetsAccessibilityVerifier,
                  TagsAccessibilityVerifier tagsAccessibilityVerifier) {

        this.actorExtractor = actorExtractor;
        this.tagSetsAccessibilityVerifier = tagSetsAccessibilityVerifier;
        this.tagsAccessibilityVerifier = tagsAccessibilityVerifier;
    }

    public TagSet create(String name, Collection<TagId> tags) {
        var creator = actorExtractor.getActor();
        var validator = new TagSetValidator();

        var nonNullTags = requireNonNullElse(tags, new ArrayList<TagId>());
        validateTagsAccessibleFor(creator, nonNullTags);

        var tagSet = new TagSet(
                new TagSetId(),
                creator,
                name,
                nonNullTags,
                !DELETED,
                actorExtractor,
                tagSetsAccessibilityVerifier,
                tagsAccessibilityVerifier,
                validator
        );
        tagSet.validate();
        return tagSet;
    }

    public TagSet reconstitute(TagSetId id,
                               User creator,
                               String name,
                               Collection<TagId> tags,
                               boolean deleted) {

        var validator = new TagSetValidator();

        return new TagSet(
                id,
                creator,
                name,
                tags,
                deleted,
                actorExtractor,
                tagSetsAccessibilityVerifier,
                tagsAccessibilityVerifier,
                validator
        );
    }

    private void validateTagsAccessibleFor(User user, Collection<TagId> tags) {
        tagsAccessibilityVerifier.nonAccessibleFor(user, tags)
                .stream()
                .findFirst()
                .ifPresent((inaccessibleTag) -> {
                    String errorMessage = "Tag with ID %s not found".formatted(inaccessibleTag.id());
                    throw new EntityInvalidException(TagSet.class, errorMessage);
                });
    }
}
