package ovh.equino.actracker.datasource.jpa.tagset;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.tagset.TagSetDataSource;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetId;
import ovh.equino.actracker.domain.tagset.TagSetSearchCriteria;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.JpaDAO;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.*;

class JpaTagSetDataSource extends JpaDAO implements TagSetDataSource {

    JpaTagSetDataSource(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public Optional<TagSetDto> find(TagSetId tagSetId, User searcher) {

        SelectTagSetQuery selectTagSet = new SelectTagSetQuery(entityManager);
        Optional<TagSetProjection> tagSetResult =
                selectTagSet
                        .where(
                                selectTagSet.predicate().and(
                                        selectTagSet.predicate().hasId(tagSetId.id()),
                                        selectTagSet.predicate().isAccessibleFor(searcher),
                                        selectTagSet.predicate().isNotDeleted()
                                )
                        )
                        .execute();

        SelectTagSetJoinTagQuery selectTagSetJoinTag = new SelectTagSetJoinTagQuery(entityManager);
        Set<UUID> tagSetJoinTag =
                selectTagSetJoinTag
                        .where(
                                selectTagSetJoinTag.predicate().and(
                                        selectTagSetJoinTag.predicate().hasTagSetId(tagSetId.id()),
                                        selectTagSetJoinTag.predicate().isNotDeleted(),
                                        selectTagSetJoinTag.predicate().isAccessibleFor(searcher)
                                )
                        )
                        .execute()
                        .stream()
                        .map(TagSetJoinTagProjection::tagId)
                        .map(UUID::fromString)
                        .collect(toUnmodifiableSet());

        return tagSetResult.map(result -> result.toTagSet(tagSetJoinTag));
    }

    @Override
    public List<TagSetDto> find(TagSetSearchCriteria searchCriteria) {

        var selectTagSets = new SelectTagSetsQuery(entityManager);
        var orderCriteria = selectTagSets.order().from(searchCriteria.common().sortCriteria());

        var tagSetResults = selectTagSets
                .where(
                        selectTagSets.predicate().and(
                                selectTagSets.predicate().isAccessibleFor(searchCriteria.common().searcher()),
                                selectTagSets.predicate().isNotDeleted(),
                                selectTagSets.predicate().isInPage(searchCriteria.common().pageId()),
                                selectTagSets.predicate().isNotExcluded(searchCriteria.excludeFilter())
                        )
                )
                .orderBy(orderCriteria)
                .limit(searchCriteria.common().pageSize())
                .execute();

        var foundTagSetIds = tagSetResults
                .stream()
                .map(TagSetProjection::id)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());

        var selectTagSetJoinTag = new SelectTagSetJoinTagQuery(entityManager);
        var tagIdsByTagSetId =
                selectTagSetJoinTag
                        .where(
                                selectTagSetJoinTag.predicate().and(
                                        selectTagSetJoinTag.predicate().hasTagSetIdIn(foundTagSetIds),
                                        selectTagSetJoinTag.predicate().isNotDeleted(),
                                        selectTagSetJoinTag.predicate().isAccessibleFor(searchCriteria.common().searcher())
                                )
                        )
                        .execute()
                        .stream()
                        .collect(groupingBy(
                                TagSetJoinTagProjection::tagSetId,
                                mapping(projection -> UUID.fromString(projection.tagId()), toUnmodifiableSet())
                        ));

        return tagSetResults
                .stream()
                .map(result -> result.toTagSet(tagIdsByTagSetId.getOrDefault(result.id(), emptySet())))
                .toList();
    }
}
