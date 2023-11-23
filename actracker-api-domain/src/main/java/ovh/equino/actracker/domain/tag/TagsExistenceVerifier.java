package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.user.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableSet;

public class TagsExistenceVerifier {

    private final TagDataSource tagDataSource;
    private final User owner;

    public TagsExistenceVerifier(TagDataSource tagDataSource, User owner) {
        this.tagDataSource = tagDataSource;
        this.owner = owner;
    }

    public Set<TagId> notExisting(Collection<TagId> tags) {
        Set<TagId> existingTags = existing(tags);
        return tags.stream()
                .filter(not(existingTags::contains))
                .collect(toUnmodifiableSet());
    }

    public Set<TagId> existing(Collection<TagId> tags) {
        List<TagId> existingTags = existingTags(tags).stream()
                .map(Tag::id)
                .toList();
        return tags.stream()
                .filter(existingTags::contains)
                .collect(toUnmodifiableSet());
    }

    List<Tag> existingTags(Collection<TagId> tagsToCheck) {
        return tagDataSource.find(new HashSet<>(tagsToCheck), owner)
                .stream()
                .map(Tag::fromStorage)
                .filter(Tag::isNotDeleted)
                .toList();
    }
}
