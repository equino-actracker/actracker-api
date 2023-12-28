package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.util.Collection;
import java.util.Set;

import static java.util.Collections.emptySet;

public class TagSetTestFactory implements TagSetFactory {

    private final User user;
    private final ActorExtractor actorExtractor;
    private final TagSetsAccessibilityVerifier tagSetsAccessibilityVerifier;
    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;
    private final TagSetValidator tagSetValidator;

    public static TagSetTestFactory forUser(User user) {
        return new TagSetTestFactory(user);
    }

    private TagSetTestFactory(User user) {
        this.user = user;
        this.actorExtractor = () -> user;
        this.tagSetsAccessibilityVerifier = (user1, tagSetId) -> true;
        this.tagsAccessibilityVerifier = new TagsAccessibilityVerifier() {
            @Override
            public boolean isAccessibleFor(User user, TagId tag) {
                return true;
            }

            @Override
            public Set<TagId> nonAccessibleFor(User user, Collection<TagId> tags) {
                return emptySet();
            }
        };
        this.tagSetValidator = new TagSetValidator();
    }

    @Override
    public TagSet create(String name, Collection<TagId> tags) {
        return new TagSet(
                new TagSetId(),
                user,
                name,
                tags,
                false,
                actorExtractor,
                tagSetsAccessibilityVerifier,
                tagsAccessibilityVerifier,
                tagSetValidator
        );
    }

    @Override
    public TagSet reconstitute(TagSetId id, User creator, String name, Collection<TagId> tags, boolean deleted) {
        return new TagSet(
                id,
                creator,
                name,
                tags,
                deleted,
                actorExtractor,
                tagSetsAccessibilityVerifier,
                tagsAccessibilityVerifier,
                tagSetValidator
        );
    }
}
