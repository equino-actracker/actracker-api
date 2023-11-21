package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.user.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static java.util.stream.Stream.concat;

public final class IntegrationTestTagSetsConfiguration {

    private final IntegrationTestTagsConfiguration tags;

    private final List<TagSetDto> addedTagSets = new ArrayList<>();
    private final List<TagSetDto> transientTagSets = new ArrayList<>();

    IntegrationTestTagSetsConfiguration(IntegrationTestTagsConfiguration tags) {
        this.tags = tags;
    }

    void persistIn(IntegrationTestRelationalDataBase dataBase) throws SQLException {
        dataBase.addTagSets(addedTagSets.toArray(new TagSetDto[0]));
    }

    public void add(TagSetDto tagSet) {
        addedTagSets.add(tagSet);
    }

    public void addTransient(TagSetDto tagSet) {
        transientTagSets.add(tagSet);
    }

    public List<TagSetDto> accessibleFor(User user) {
        return addedTagSets
                .stream()
                .filter(not(TagSetDto::deleted))
                .filter(tagSet -> isOwner(user, tagSet))
                .map(tagSet -> toAccessibleFormFor(user, tagSet))
                .sorted(comparing(tagSet -> tagSet.id().toString()))
                .toList();
    }

    public List<TagSetDto> inaccessibleFor(User user) {
        List<UUID> accessibleTagSets = accessibleFor(user)
                .stream()
                .map(TagSetDto::id)
                .toList();
        return concat(addedTagSets.stream(), transientTagSets.stream())
                .filter(tagSet -> !accessibleTagSets.contains(tagSet.id()))
                .toList();
    }

    private boolean isOwner(User user, TagSetDto tagSet) {
        return tagSet.creatorId().equals(user.id());
    }

    private TagSetDto toAccessibleFormFor(User user, TagSetDto tagSet) {
        return new TagSetDto(
                tagSet.id(),
                tagSet.creatorId(),
                tagSet.name(),
                getIncludedAccessibleTags(user, tagSet),
                tagSet.deleted()
        );
    }

    private Set<UUID> getIncludedAccessibleTags(User user, TagSetDto tagSet) {
        Set<UUID> accessibleTagIds = tags.accessibleFor(user)
                .stream()
                .map(TagDto::id)
                .collect(toUnmodifiableSet());
        return tagSet.tags()
                .stream()
                .filter(accessibleTagIds::contains)
                .collect(toUnmodifiableSet());
    }
}
