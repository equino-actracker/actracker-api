package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.tagset.TagSetDataSource;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaDAO;
import ovh.equino.actracker.repository.jpa.tag.TagEntity;

import java.util.List;
import java.util.Optional;

import static jakarta.persistence.criteria.JoinType.LEFT;

class JpaTagSetDataSource extends JpaDAO implements TagSetDataSource {

    private final TagSetMapper mapper = new TagSetMapper();

    @Override
    public Optional<TagSetDto> find(TagSetId tagSetId, User searcher) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();

        Root<TagSetEntity> tagSet = criteriaQuery.from(TagSetEntity.class);
        Join<TagSetEntity, TagEntity> tag = tagSet.join("tags", LEFT);

        // TODO replace get by string with generated JPA Meta Model
        Predicate hasIdAsRequested = criteriaBuilder.equal(tagSet.get("id"), tagSetId.id().toString());
        Predicate isAccessibleForSearcher = criteriaBuilder.equal(tagSet.get("creatorId"), searcher.id().toString());
        Predicate isTagSetNotDeleted = criteriaBuilder.isFalse(tagSet.get("deleted"));
        Predicate isTagNotDeleted = criteriaBuilder.or(
                criteriaBuilder.isFalse(tag.get("deleted")),
                criteriaBuilder.isNull(tag.get("id"))
        );

        CriteriaQuery<Tuple> query = criteriaQuery.multiselect(tagSet.alias("tagSetAlias"))
                .where(
                        criteriaBuilder.and(
                                hasIdAsRequested,
                                isAccessibleForSearcher,
                                isTagSetNotDeleted,
                                isTagNotDeleted
                        )
                );
        return entityManager.createQuery(query)
                .getResultStream()
                .findFirst()
                .map(result -> result.get("tagSetAlias", TagSetEntity.class))
                .map(mapper::toDto);
    }

    @Override
    public List<TagSetDto> find(EntitySearchCriteria searchCriteria) {


        return null;
    }
}
