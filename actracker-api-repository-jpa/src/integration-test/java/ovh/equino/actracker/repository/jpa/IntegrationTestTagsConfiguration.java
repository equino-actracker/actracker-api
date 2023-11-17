package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.user.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.function.Predicate.not;
import static java.util.stream.Stream.concat;

public class IntegrationTestTagsConfiguration {

    private final List<TagDto> addedTags = new ArrayList<>();
    private final List<TagDto> transientTags = new ArrayList<>();

    void persistIn(IntegrationTestRelationalDataBase database) throws SQLException {
        database.addTags(addedTags.toArray(new TagDto[0]));
    }

    public void add(TagDto tag) {
        addedTags.add(tag);
    }

    public void addTransient(TagDto tag) {
        transientTags.add(tag);
    }

    public Collection<TagDto> accessibleFor(User user) {
        return addedTags
                .stream()
                .filter(not(TagDto::deleted))
                .filter(tag -> isOwnerOrGrantee(user, tag))
                .map(tag -> toAccessibleFormFor(user, tag))
                .sorted(comparing(tag -> tag.id().toString()))
                .toList();
    }

    public Collection<TagDto> inaccessibleFor(User user) {
        List<UUID> accessibleTags = accessibleFor(user)
                .stream()
                .map(TagDto::id)
                .toList();
        return concat(addedTags.stream(), transientTags.stream())
                .filter(tag -> !accessibleTags.contains(tag.id()))
                .toList();
    }

    private boolean isOwnerOrGrantee(User user, TagDto tag) {
        return isOwner(user, tag) || isGrantee(user, tag);
    }

    private boolean isGrantee(User user, TagDto tag) {
        List<User> grantees = tag.shares()
                .stream()
                .map(Share::grantee)
                .toList();
        return grantees.contains(user);
    }

    private boolean isOwner(User user, TagDto tag) {
        return user.id().equals(tag.creatorId());
    }

    private TagDto toAccessibleFormFor(User user, TagDto tag) {
        List<Share> shares = isOwner(user, tag)
                ? tag.shares()
                : emptyList();
        List<MetricDto> metrics = tag.metrics()
                .stream()
                .filter(not(MetricDto::deleted))
                .toList();
        return new TagDto(
                tag.id(),
                tag.creatorId(),
                tag.name(),
                metrics,
                shares,
                tag.deleted()
        );
    }
}
