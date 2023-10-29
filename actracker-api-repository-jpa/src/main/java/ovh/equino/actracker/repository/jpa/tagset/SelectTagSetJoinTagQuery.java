package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import ovh.equino.actracker.domain.tagset.TagSetId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaPredicate;
import ovh.equino.actracker.repository.jpa.JpaQuery;

import java.util.List;

import static jakarta.persistence.criteria.JoinType.INNER;
import static java.util.Arrays.stream;

class SelectTagSetJoinTagQuery implements JpaQuery<TagSetEntity, List<TagSetJoinTagProjection>> {

    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private final CriteriaQuery<TagSetJoinTagProjection> query;
    private final Root<TagSetEntity> tagSet;
    private final Join<TagSetEntity, ?> tag;

    SelectTagSetJoinTagQuery(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.query = criteriaBuilder.createQuery(TagSetJoinTagProjection.class);
        this.tagSet = query.from(TagSetEntity.class);
        this.tag = tagSet.join("tags", INNER);

        query.select(
                criteriaBuilder.construct(
                        TagSetJoinTagProjection.class,
                        tag.get("id"),
                        tagSet.get("id")
                )
        );
    }

    @Override
    public JpaQuery<TagSetEntity, List<TagSetJoinTagProjection>> where(JpaPredicate... conditions) {
        Predicate[] predicates = stream(conditions)
                .map(JpaPredicate::toJpa)
                .toArray(Predicate[]::new);
        query.where(predicates);
        return this;
    }

    @Override
    public List<TagSetJoinTagProjection> execute() {
        return entityManager.createQuery(query).getResultList();
    }

    public JpaPredicate assignedForTagSet(TagSetId tagSetId) {
        return () -> criteriaBuilder.equal(tagSet.get("id"), tagSetId.id().toString());
    }

    public JpaPredicate isNotDeleted() {
        return () -> criteriaBuilder.and(
                criteriaBuilder.isFalse(tagSet.get("deleted")),
                criteriaBuilder.isFalse(tag.get("deleted"))
        );
    }

    public JpaPredicate isAccessibleFor(User user) {
        return () -> criteriaBuilder.and(
                criteriaBuilder.equal(tagSet.get("creatorId"), user.id().toString()),
                criteriaBuilder.equal(tag.get("creatorId"), user.id().toString())
        );
    }
}
