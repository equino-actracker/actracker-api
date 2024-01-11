package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.tag.TagEntity;
import ovh.equino.actracker.jpa.tag.TagShareEntity;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.SingleResultJpaQuery;

class SelectTagQuery extends SingleResultJpaQuery<TagEntity, TagProjection> {

    private final PredicateBuilder predicate;

    protected SelectTagQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicate = new PredicateBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
                this.criteriaBuilder.construct(
                        TagProjection.class,
                        root.get("id"),
                        root.get("creatorId"),
                        root.get("name"),
                        root.get("deleted")
                )
        );
    }

    @Override
    public PredicateBuilder predicate() {
        return this.predicate;
    }

    @Override
    public SelectTagQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    @Override
    protected Class<TagEntity> getRootEntityType() {
        return TagEntity.class;
    }

    @Override
    protected Class<TagProjection> getProjectionType() {
        return TagProjection.class;
    }

    public class PredicateBuilder extends JpaPredicateBuilder<TagEntity> {
        private PredicateBuilder() {
            super(criteriaBuilder, root);
        }

        @Override
        public JpaPredicate isAccessibleFor(User searcher) {
            return or(
                    super.isAccessibleFor(searcher),
                    isGrantee(searcher)
            );
        }

        private JpaPredicate isGrantee(User user) {
            Join<TagEntity, TagShareEntity> shares = root.join("shares", JoinType.LEFT);
            Subquery<Long> subQuery = query.subquery(Long.class);
            subQuery.select(criteriaBuilder.literal(1L))
                    .where(
                            criteriaBuilder.and(
                                    criteriaBuilder.equal(shares.get("granteeId"), user.id().toString())
                            )
                    )
                    .from(TagEntity.class);
            return () -> criteriaBuilder.exists(subQuery);
        }
    }
}
