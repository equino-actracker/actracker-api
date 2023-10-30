package ovh.equino.actracker.repository.jpa.tagset;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.tagset.TagSetDataSource;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.*;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.*;

class JpaTagSetDataSource extends JpaDAO implements TagSetDataSource {

    @Override
    public Optional<TagSetDto> find(TagSetId tagSetId, User searcher) {

        SelectTagSetQuery selectTagSet = new SelectTagSetQuery(entityManager);
        SelectTagSetQuery.PredicateBuilder tagSetCriteria = selectTagSet.predicateBuilder();
        Optional<TagSetProjection> tagSetResult =
                selectTagSet
                        .where(
                                tagSetCriteria.and(
                                        tagSetCriteria.hasId(tagSetId.id()),
                                        tagSetCriteria.isAccessibleFor(searcher),
                                        tagSetCriteria.isNotDeleted()
                                )
                        )
                        .execute();

        SelectTagSetJoinTagQuery selectTagSetJoinTag = new SelectTagSetJoinTagQuery(entityManager);
        SelectTagSetJoinTagQuery.PredicateBuilder tagCriteria = selectTagSetJoinTag.predicateBuilder();
        List<TagSetJoinTagProjection> tagSetJoinTag =
                selectTagSetJoinTag
                        .where(
                                tagCriteria.and(
                                        tagCriteria.assignedForTagSet(tagSetId.id()),
                                        tagCriteria.isNotDeleted(),
                                        tagCriteria.isAccessibleFor(searcher)
                                )
                        )
                        .execute();

        Map<String, Set<UUID>> tagIdsByTagSetId = tagSetJoinTag
                .stream()
                .collect(groupingBy(
                        TagSetJoinTagProjection::tagSetId,
                        mapping(projection -> UUID.fromString(projection.tagId()), toUnmodifiableSet())
                ));

        return tagSetResult
                .map(result -> toTagSet(
                        result,
                        tagIdsByTagSetId.getOrDefault(result.id(), emptySet())
                ));
    }

    @Override
    public List<TagSetDto> find(EntitySearchCriteria searchCriteria) {
        return null;
    }

    private TagSetDto toTagSet(TagSetProjection tagSetProjection, Set<UUID> tagIds) {
        return new TagSetDto(
                UUID.fromString(tagSetProjection.id()),
                UUID.fromString(tagSetProjection.creatorId()),
                tagSetProjection.name(),
                tagIds,
                tagSetProjection.deleted()
        );
    }

}
