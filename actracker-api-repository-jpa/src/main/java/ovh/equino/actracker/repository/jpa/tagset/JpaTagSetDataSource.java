package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.tagset.TagSetDataSource;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.*;

import static jakarta.persistence.criteria.JoinType.LEFT;
import static java.util.Collections.emptySet;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.*;

class JpaTagSetDataSource extends JpaDAO implements TagSetDataSource {

    @Override
    public Optional<TagSetDto> find(TagSetId tagSetId, User searcher) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> tupleQuery = criteriaBuilder.createTupleQuery();
        Root<TagSetEntity> root = tupleQuery.from(TagSetEntity.class);
        Join<Object, Object> tagSetTags = root.join("tags", LEFT);

        // TODO replace with generated JPA Meta Model
        tupleQuery.select(criteriaBuilder.tuple(
                root.get("id").alias("id"),
                root.get("creatorId").alias("creatorId"),
                root.get("name").alias("name"),
                root.get("deleted").alias("deleted"),
                tagSetTags.get("id").alias("tag_id")
        ));

        List<Tuple> results = entityManager
                .createQuery(tupleQuery)
                .getResultList();
        List<TagSetDto> tagSets = toTagSets(results);

        return tagSets.stream().findFirst();
    }

    @Override
    public List<TagSetDto> find(EntitySearchCriteria searchCriteria) {


        return null;
    }

    private List<TagSetDto> toTagSets(List<Tuple> queryResults) {
        Map<UUID, Set<UUID>> tagIdsByTagSetId = toTagIdsByTagSetId(queryResults);
        return queryResults
                .stream()
                .map(row -> toTagSet(row, tagIdsByTagSetId))
                .distinct()
                .toList();
    }

    private Map<UUID, Set<UUID>> toTagIdsByTagSetId(List<Tuple> queryResults) {
        return queryResults
                .stream()
                .filter(row -> nonNull(row.get("tag_id", String.class)))
                .collect(
                        groupingBy(
                                row -> UUID.fromString(row.get("id", String.class)),
                                mapping(this::toTagId, toSet())
                        )
                );
    }

    private UUID toTagId(Tuple queryResult) {
        return UUID.fromString(queryResult.get("tag_id", String.class));
    }

    private TagSetDto toTagSet(Tuple queryResult, Map<UUID, Set<UUID>> tagIdsByTagSetId) {
        UUID tagSetId = UUID.fromString(queryResult.get("id", String.class));
        return new TagSetDto(
                tagSetId,
                UUID.fromString(queryResult.get("creatorId", String.class)),
                queryResult.get("name", String.class),
                tagIdsByTagSetId.getOrDefault(tagSetId, emptySet()),
                queryResult.get("deleted", Boolean.class)
        );
    }
}
