package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.criteria.*;
import org.apache.commons.lang3.StringUtils;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.tagset.TagSetDataSource;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaDAO;
import ovh.equino.actracker.repository.jpa.tag.TagEntity;

import java.util.*;

import static jakarta.persistence.criteria.JoinType.INNER;
import static java.util.stream.Collectors.toUnmodifiableSet;

class JpaTagSetDataSource extends JpaDAO implements TagSetDataSource {

    private final TagSetMapper mapper = new TagSetMapper();

    @Override
    public Optional<TagSetDto> find(TagSetId tagSetId, User searcher) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        // TODO replace get by string with generated JPA Meta Model
        CriteriaQuery<TagSetProjection> tagSetQuery = criteriaBuilder.createQuery(TagSetProjection.class);
        Root<TagSetEntity> tagSet = tagSetQuery.from(TagSetEntity.class);
        Predicate hasIdAsRequested = criteriaBuilder.equal(tagSet.get("id"), tagSetId.id().toString());
        Predicate isAccessibleForSearcher = criteriaBuilder.equal(tagSet.get("creatorId"), searcher.id().toString());
        Predicate isTagSetNotDeleted = criteriaBuilder.isFalse(tagSet.get("deleted"));

        tagSetQuery
                .select(
                        criteriaBuilder.construct(
                                TagSetProjection.class,
                                tagSet.get("id"),
                                tagSet.get("creatorId"),
                                tagSet.get("name"),
                                tagSet.get("deleted")
                        )
                )
                .where(
                        criteriaBuilder.and(
                                hasIdAsRequested,
                                isAccessibleForSearcher,
                                isTagSetNotDeleted
                        )
                );


        Optional<TagSetProjection> tagSetResult = entityManager.createQuery(tagSetQuery)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();

        CriteriaQuery<TagProjection> tagSetTagQuery = criteriaBuilder.createQuery(TagProjection.class);
        Root<TagSetEntity> tagSetTag = tagSetTagQuery.from(TagSetEntity.class);
        Join<TagSetEntity, TagEntity> tag = tagSetTag.join("tags", INNER);
        Predicate isTagNotDeleted = criteriaBuilder.isFalse(tag.get("deleted"));
        Predicate isTagForTagSet = criteriaBuilder.equal(tagSetTag.get("id"), tagSetId.id().toString());

        tagSetTagQuery
                .select(
                        criteriaBuilder.construct(
                                TagProjection.class,
                                tag.get("id"),
                                tagSetTag.get("id")
                        )
                )
                .where(
                        criteriaBuilder.and(
                                isTagNotDeleted,
                                isTagForTagSet
                        )
                );

        List<TagProjection> tagsResults = entityManager.createQuery(tagSetTagQuery)
                .getResultList();


        return tagSetResult
                .map(result -> toTagSet(
                        result,
                        extractTagIdsFor(result, tagsResults)
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

    private Set<UUID> extractTagIdsFor(TagSetProjection tagSetProjection,
                                       Collection<TagProjection> tagProjections) {

        return tagProjections
                .stream()
                .filter(tagProjection -> StringUtils.equals(tagProjection.tagSetId(), tagSetProjection.id()))
                .map(TagProjection::tagId)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());
    }

}
