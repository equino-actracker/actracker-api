package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import ovh.equino.actracker.repository.jpa.JpaPredicate;
import ovh.equino.actracker.repository.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.repository.jpa.JpaSortBuilder;
import ovh.equino.actracker.repository.jpa.MultiResultJpaQuery;

import java.util.UUID;

import static jakarta.persistence.criteria.JoinType.INNER;

final class SelectShareJoinTagQuery extends MultiResultJpaQuery<TagShareEntity, ShareJoinTagProjection> {

    private final Join<TagShareEntity, TagEntity> tag;
    private final PredicateBuilder predicate;

    SelectShareJoinTagQuery(EntityManager entityManager) {
        super(entityManager);
        this.tag = root.join("tag", INNER);
        this.predicate = new PredicateBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
                criteriaBuilder.construct(
                        ShareJoinTagProjection.class,
                        root.get("granteeId"),
                        tag.get("id"),
                        root.get("granteeName")
                )
        );
    }

    @Override
    public PredicateBuilder predicate() {
        return predicate;
    }

    @Override
    public SelectShareJoinTagQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    /**
     * Deprecated: Sorting this entity is not supported. An attempt will throw RuntimeException.
     */
    @Override
    @Deprecated
    public JpaSortBuilder<TagShareEntity> sort() {
        throw new RuntimeException("Sorting shares joint with tags not supported");
    }

    @Override
    protected Class<TagShareEntity> getRootEntityType() {
        return TagShareEntity.class;
    }

    @Override
    protected Class<ShareJoinTagProjection> getProjectionType() {
        return ShareJoinTagProjection.class;
    }

    class PredicateBuilder extends JpaPredicateBuilder<TagShareEntity> {
        private PredicateBuilder() {
            super(criteriaBuilder, root);
        }

        @Override
        public JpaPredicate isNotDeleted() {
            return () -> criteriaBuilder.isFalse(tag.get("deleted"));
        }

        public JpaPredicate hasTagId(UUID tagId) {
            return () -> criteriaBuilder.equal(tag.get("id"), tagId.toString());
        }
    }
}
