package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.tag.TagDataSource;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.User;

import java.util.Collection;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
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

        var tagSetAccessibilityVerifier = new TagSetsAccessibilityVerifier(tagSetDataSource, creator);
        var tagsAccessibilityVerifier = new TagsAccessibilityVerifier(tagDataSource, creator);
        var validator = new TagSetValidator(tagsAccessibilityVerifier);

        var tagSet = new TagSet(
                new TagSetId(),
                creator, name,
                requireNonNullElse(tags, emptyList()),
                !DELETED,
                validator,
                tagSetAccessibilityVerifier,
                tagsAccessibilityVerifier
        );
        tagSet.validate();
        return tagSet;
    }

    public TagSet reconstitute(User actor,
                               TagSetId id,
                               User creator,
                               String name,
                               Collection<TagId> tags,
                               boolean deleted) {

        var tagSetAccessibilityVerifier = new TagSetsAccessibilityVerifier(tagSetDataSource, actor);
        var tagsAccessibilityVerifier = new TagsAccessibilityVerifier(tagDataSource, actor);
        var validator = new TagSetValidator(tagsAccessibilityVerifier);

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
}
