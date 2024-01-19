package ovh.equino.actracker.datasource.jpa.dashboard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.dashboard.DashboardEntity;
import ovh.equino.actracker.jpa.dashboard.DashboardShareEntity;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.SingleResultJpaQuery;

final class SelectDashboardQuery extends SingleResultJpaQuery<DashboardEntity, DashboardProjection> {

    private final PredicateBuilder predicate;

    SelectDashboardQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicate = new PredicateBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
                criteriaBuilder.construct(
                        DashboardProjection.class,
                        root.get("id"),
                        root.get("creatorId"),
                        root.get("name"),
                        root.get("deleted")
                )
        );
    }

    @Override
    public PredicateBuilder predicate() {
        return predicate;
    }

    @Override
    public SelectDashboardQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    @Override
    protected Class<DashboardEntity> getRootEntityType() {
        return DashboardEntity.class;
    }

    @Override
    protected Class<DashboardProjection> getProjectionType() {
        return DashboardProjection.class;
    }

    public final class PredicateBuilder extends JpaPredicateBuilder<DashboardEntity> {
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
            Join<DashboardEntity, DashboardShareEntity> shares = root.join("shares", JoinType.LEFT);
            Subquery<Long> subQuery = query.subquery(Long.class);
            subQuery.select(criteriaBuilder.literal(1L))
                    .where(
                            criteriaBuilder.and(
                                    criteriaBuilder.equal(shares.get("granteeId"), user.id().toString())
                            )
                    )
                    .from(DashboardEntity.class);
            return () -> criteriaBuilder.exists(subQuery);
        }
    }
}
