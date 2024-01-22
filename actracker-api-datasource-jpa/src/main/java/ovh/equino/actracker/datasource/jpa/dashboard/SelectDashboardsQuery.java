package ovh.equino.actracker.datasource.jpa.dashboard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.JpaSortBuilder;
import ovh.equino.actracker.datasource.jpa.MultiResultJpaQuery;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.dashboard.DashboardEntity;
import ovh.equino.actracker.jpa.dashboard.DashboardEntity_;
import ovh.equino.actracker.jpa.dashboard.DashboardShareEntity;
import ovh.equino.actracker.jpa.dashboard.DashboardShareEntity_;

final class SelectDashboardsQuery extends MultiResultJpaQuery<DashboardEntity, DashboardProjection> {

    private final PredicateBuilder predicate;
    private final SortBuilder sort;

    SelectDashboardsQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicate = new PredicateBuilder();
        this.sort = new SortBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
                        criteriaBuilder.construct(
                                DashboardProjection.class,
                                root.get(DashboardEntity_.id),
                                root.get(DashboardEntity_.creatorId),
                                root.get(DashboardEntity_.name),
                                root.get(DashboardEntity_.deleted)
                        )
                )
                .distinct(true);
    }

    @Override
    public PredicateBuilder predicate() {
        return predicate;
    }

    @Override
    public SelectDashboardsQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    @Override
    public SortBuilder sort() {
        return sort;
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

        public JpaPredicate isNotDeleted() {
            return () -> criteriaBuilder.isFalse(root.get(DashboardEntity_.deleted));
        }

        public JpaPredicate isAccessibleFor(User searcher) {
            return or(
                    isOwner(searcher),
                    isGrantee(searcher)
            );
        }

        public JpaPredicate isOwner(User searcher) {
            return () -> criteriaBuilder.equal(
                    root.get(DashboardEntity_.creatorId),
                    searcher.id().toString()
            );
        }

        private JpaPredicate isGrantee(User user) {
            Join<DashboardEntity, DashboardShareEntity> sharedDashboard = root.join(DashboardEntity_.shares, JoinType.LEFT);
            Subquery<Long> subQuery = query.subquery(Long.class);
            subQuery.select(criteriaBuilder.literal(1L))
                    .where(criteriaBuilder.equal(sharedDashboard.get(DashboardShareEntity_.granteeId), user.id().toString()))
                    .from(DashboardEntity.class);
            return () -> criteriaBuilder.exists(subQuery);
        }
    }

    public final class SortBuilder extends JpaSortBuilder<DashboardEntity> {
        private SortBuilder() {
            super(criteriaBuilder, root);
        }
    }
}
