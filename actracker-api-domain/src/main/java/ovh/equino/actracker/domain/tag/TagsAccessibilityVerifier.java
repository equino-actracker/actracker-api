package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.user.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableSet;

public class TagsAccessibilityVerifier {

    private final TagDataSource tagDataSource;

    TagsAccessibilityVerifier(TagDataSource tagDataSource) {
        this.tagDataSource = tagDataSource;
    }

    public boolean isAccessibleFor(User user, TagId tag) {
        return tagDataSource.find(tag, user).isPresent();
    }

    public Set<TagId> nonAccessibleFor(User user, Collection<TagId> tags) {
        Set<TagId> accessibleTags = accessibleFor(user, tags);
        return tags.stream()
                .filter(not(accessibleTags::contains))
                .collect(toUnmodifiableSet());
    }

    private Set<TagId> accessibleFor(User user, Collection<TagId> tags) {
        Set<TagId> accessibleTags = tagDataSource.find(new HashSet<>(tags), user)
                .stream()
                .map(TagDto::id)
                .map(TagId::new)
                .collect(toUnmodifiableSet());
        return tags.stream()
                .filter(accessibleTags::contains)
                .collect(toUnmodifiableSet());
    }
}
