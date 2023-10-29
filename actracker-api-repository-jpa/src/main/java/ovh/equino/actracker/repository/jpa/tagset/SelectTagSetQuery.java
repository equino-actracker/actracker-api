package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import ovh.equino.actracker.domain.tagset.TagSetId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaPredicate;
import ovh.equino.actracker.repository.jpa.JpaQuery;

import java.util.Optional;

import static java.util.Arrays.stream;

class SelectTagSetQuery implements JpaQuery<TagSetEntity, Optional<TagSetProjection>> {

    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private final CriteriaQuery<TagSetProjection> query;
    private final Root<TagSetEntity> tagSet;

    SelectTagSetQuery(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.query = criteriaBuilder.createQuery(TagSetProjection.class);
        this.tagSet = query.from(TagSetEntity.class);

        query.select(
                criteriaBuilder.construct(
                        TagSetProjection.class,
                        tagSet.get("id"),
                        tagSet.get("creatorId"),
                        tagSet.get("name"),
                        tagSet.get("deleted")
                )
        );
    }

    @Override
    public JpaQuery<TagSetEntity, Optional<TagSetProjection>> where(JpaPredicate... conditions) {
        Predicate[] predicates = stream(conditions)
                .map(JpaPredicate::toJpa)
                .toArray(Predicate[]::new);
        query.where(predicates);
        return this;
    }

    @Override
    public Optional<TagSetProjection> execute() {
        return entityManager.createQuery(query)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    JpaPredicate hasId(TagSetId id) {
        return () -> criteriaBuilder.equal(tagSet.get("id"), id.id().toString());
    }

    JpaPredicate isAccessibleFor(User searcher) {
        return () -> criteriaBuilder.equal(tagSet.get("creatorId"), searcher.id().toString());
    }

    JpaPredicate isNotDeleted() {
        return () -> criteriaBuilder.isFalse(tagSet.get("deleted"));
    }
}
