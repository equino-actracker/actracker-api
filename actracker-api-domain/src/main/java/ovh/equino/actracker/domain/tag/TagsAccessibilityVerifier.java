package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.user.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableSet;

public class TagsAccessibilityVerifier {

    private final TagDataSource tagDataSource;
    // TODO should have identity provider? Or maybe user provided for each method as argument?
    private final User user;

    public TagsAccessibilityVerifier(TagDataSource tagDataSource, User user) {
        this.tagDataSource = tagDataSource;
        this.user = user;
    }

    public Set<TagId> nonAccessibleOf(Collection<TagId> tags) {
        Set<TagId> accessibleTags = accessibleOf(tags);
        return tags.stream()
                .filter(not(accessibleTags::contains))
                .collect(toUnmodifiableSet());
    }

    public Set<TagId> accessibleOf(Collection<TagId> tags) {
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
