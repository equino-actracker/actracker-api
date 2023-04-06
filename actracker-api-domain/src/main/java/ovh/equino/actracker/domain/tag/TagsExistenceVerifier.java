package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.user.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableSet;

public class TagsExistenceVerifier {

    private final TagRepository tagRepository;
    private final User owner;

    public TagsExistenceVerifier(TagRepository tagRepository, User owner) {
        this.tagRepository = tagRepository;
        this.owner = owner;
    }

    public Set<TagId> notExisting(Collection<TagId> tags) {
        List<TagId> availableTags = availableTagIds();
        return tags.stream()
                .filter(not(availableTags::contains))
                .collect(toUnmodifiableSet());
    }

    public Set<TagId> existing(Collection<TagId> tags) {
        List<TagId> availableTags = availableTagIds();
        return tags.stream()
                .filter(availableTags::contains)
                .collect(toUnmodifiableSet());
    }

    private List<TagId> availableTagIds() {
        return tagRepository.findAll(owner).stream()
                .map(Tag::fromStorage)
                .map(Tag::id)
                .toList();
    }
}
