package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaPredicate;
import ovh.equino.actracker.repository.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.repository.jpa.JpaSortBuilder;
import ovh.equino.actracker.repository.jpa.MultiResultJpaQuery;

final class SelectActivitiesQuery extends MultiResultJpaQuery<ActivityEntity, ActivityProjection> {

    private final PredicateBuilder predicateBuilder;
    private final SortBuilder sortBuilder;

    SelectActivitiesQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicateBuilder = new PredicateBuilder();
        this.sortBuilder = new SortBuilder();
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
                )
                .distinct(true);
    }

    @Override
    public PredicateBuilder predicate() {
        return predicateBuilder;
    }

    @Override
    public SortBuilder sort() {
        return sortBuilder;
    }

    @Override
    public SelectActivitiesQuery where(JpaPredicate predicate) {
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

        @Override
        public JpaPredicate isAccessibleFor(User searcher) {
            return or(
                    super.isAccessibleFor(searcher),
                    isGrantee(searcher)
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

    public class SortBuilder extends JpaSortBuilder<ActivityEntity> {
        private SortBuilder() {
            super(criteriaBuilder, root);
        }
    }
}
