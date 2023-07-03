package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.user.User;

import java.util.Collection;
import java.util.List;

public class SharedTagsExistenceVerifier {

    private final TagsExistenceVerifier tagsExistenceVerifier;
    private final User user;

    public SharedTagsExistenceVerifier(TagsExistenceVerifier tagsExistenceVerifier, User user) {
        this.tagsExistenceVerifier = tagsExistenceVerifier;
        this.user = user;
    }

    public boolean containsSharedTags(Collection<TagId> tags) {
        List<Tag> existingTags = tagsExistenceVerifier.existingTags(tags);
        return existingTags.stream()
                .anyMatch(tag -> tag.isAccessibleFor(user));
    }
}
