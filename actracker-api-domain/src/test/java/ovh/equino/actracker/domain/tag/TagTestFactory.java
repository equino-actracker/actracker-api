package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.util.Collection;
import java.util.Set;

import static java.util.Collections.emptySet;

public class TagTestFactory implements TagFactory {

    private final User user;
    private final ActorExtractor actorExtractor;
    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;
    private final TagValidator tagValidator;

    private static TagFactory forUser(User user) {
        return new TagTestFactory(user);
    }

    private TagTestFactory(User user) {
        this.user = user;
        this.actorExtractor = () -> user;
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
        this.tagValidator = new TagValidator();
    }

    @Override
    public Tag create(String name, Collection<Metric> metrics, Collection<Share> shares) {
        return new Tag(
                new TagId(),
                user,
                name,
                metrics,
                shares,
                false,
                actorExtractor,
                tagsAccessibilityVerifier,
                tagValidator
        );
    }

    @Override
    public Tag reconstitute(TagId id,
                            User creator,
                            String name,
                            Collection<Metric> metrics,
                            Collection<Share> shares,
                            boolean deleted) {

        return new Tag(
                id,
                user,
                name,
                metrics,
                shares,
                deleted,
                actorExtractor,
                tagsAccessibilityVerifier,
                tagValidator
        );
    }
}
