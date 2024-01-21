package ovh.equino.actracker.datasource.jpa.activity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.SingleResultJpaQuery;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.activity.ActivityEntity;

final class SelectActivityQuery extends SingleResultJpaQuery<ActivityEntity, ActivityProjection> {

    private final PredicateBuilder predicateBuilder;

    SelectActivityQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicateBuilder = new PredicateBuilder();
    }

    @Override
    protected void initProjection() {
        query
                .select(
                        this.criteriaBuilder.construct(
                                ActivityProjection.class,
                                root.get("id"),
                                root.get("creatorId"),
                                root.get("title"),
                                root.get("startTime"),
                                root.get("endTime"),
                                root.get("comment"),
                                root.get("deleted")
                        )
                );
    }

    @Override
    public PredicateBuilder predicate() {
        return predicateBuilder;
    }

    @Override
    public SelectActivityQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    @Override
    protected Class<ActivityEntity> getRootEntityType() {
        return ActivityEntity.class;
    }

    @Override
    protected Class<ActivityProjection> getProjectionType() {
        return ActivityProjection.class;
    }

    public class PredicateBuilder extends JpaPredicateBuilder<ActivityEntity> {
        private PredicateBuilder() {
            super(criteriaBuilder, root);
        }

        public JpaPredicate isNotDeleted() {
            return () -> criteriaBuilder.isFalse(root.get("deleted"));
        }

        public JpaPredicate isAccessibleFor(User searcher) {
            return or(
                    isOwner(searcher),
                    isGrantee(searcher)
            );
        }

        private JpaPredicate isOwner(User searcher) {
            return () -> criteriaBuilder.equal(
                    root.get("creatorId"),
                    searcher.id().toString()
            );
        }

        private JpaPredicate isGrantee(User user) {
            Join<ActivityEntity, ?> tags = root.join("tags", JoinType.LEFT);
            Join<?, ?> shares = tags.join("shares", JoinType.LEFT);
            Subquery<Long> subQuery = query.subquery(Long.class);
            subQuery.select(criteriaBuilder.literal(1L))
                    .where(
                            criteriaBuilder.and(
                                    criteriaBuilder.equal(shares.get("granteeId"), user.id().toString()),
                                    criteriaBuilder.isFalse(tags.get("deleted"))
                            )
                    )
                    .from(ActivityEntity.class);
            return () -> criteriaBuilder.exists(subQuery);
        }
    }
}
