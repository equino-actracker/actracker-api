package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.user.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
        Set<TagId> existingTags = existing(tags);
        return tags.stream()
                .filter(not(existingTags::contains))
                .collect(toUnmodifiableSet());
    }

    public Set<TagId> existing(Collection<TagId> tags) {
        List<TagId> existingTags = existingTagIds(tags);
        return tags.stream()
                .filter(existingTags::contains)
                .collect(toUnmodifiableSet());
    }

    private List<TagId> existingTagIds(Collection<TagId> tagsToCheck) {
        Set<UUID> uuids = tagsToCheck.stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());
        return tagRepository.findByIds(uuids, owner).stream()
                .map(Tag::fromStorage)
                .filter(Tag::isNotDeleted)
                .map(Tag::id)
                .toList();
    }
}
