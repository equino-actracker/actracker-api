package ovh.equino.actracker.datasource.jpa.dashboard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import ovh.equino.actracker.datasource.jpa.*;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.dashboard.DashboardSearchCriteria;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.dashboard.DashboardEntity;
import ovh.equino.actracker.jpa.dashboard.DashboardEntity_;
import ovh.equino.actracker.jpa.dashboard.DashboardShareEntity;
import ovh.equino.actracker.jpa.dashboard.DashboardShareEntity_;

import java.util.List;

import static java.util.Collections.emptyList;

final class SelectDashboardsQuery extends MultiResultJpaQuery<DashboardEntity, DashboardProjection> {

    private final PredicateBuilder predicateBuilder;
    private final OrderBuilder orderBuilder;

    private final Expression<String> dashboardNameLowerCase;
    private final Expression<Integer> dashboardNameNullWeight;

    SelectDashboardsQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicateBuilder = new PredicateBuilder();
        this.orderBuilder = new OrderBuilder();

        this.dashboardNameLowerCase = criteriaBuilder.lower(root.get(DashboardEntity_.name));
        this.dashboardNameNullWeight = criteriaBuilder.selectCase()
                .when(criteriaBuilder.isNull(root.get(DashboardEntity_.name)), 0)
                .otherwise(1)
                .as(Integer.class);
    }

    @Override
    protected void initProjection() {
        query.select(
                        criteriaBuilder.construct(
                                DashboardProjection.class,
                                root.get(DashboardEntity_.id),
                                root.get(DashboardEntity_.creatorId),
                                root.get(DashboardEntity_.name),
                                dashboardNameLowerCase,
                                dashboardNameNullWeight,
                                root.get(DashboardEntity_.deleted)
                        )
                )
                .distinct(true);
    }

    @Override
    public PredicateBuilder predicate() {
        return predicateBuilder;
    }

    @Override
    public SelectDashboardsQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    @Override
    public OrderBuilder order() {
        return orderBuilder;
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

        @Override
        protected List<PageCondition<? extends Comparable<?>>> toEntityPageConditions(
                EntitySearchPageId.Value pageAttribute) {

            if (pageAttribute.sortField() instanceof DashboardSearchCriteria.SortableField sortableAttribute) {
                var sortDirection = pageAttribute.sortOrder();
                return switch (sortableAttribute) {
                    case NAME -> nullFirstPageConditions(
                            dashboardNameLowerCase,
                            dashboardNameNullWeight,
                            nullableStringLowerCase(pageAttribute),
                            sortDirection
                    );
                };
            }
            return emptyList();

        }
    }

    public final class OrderBuilder extends JpaOrderBuilder<DashboardEntity> {
        private OrderBuilder() {
            super(criteriaBuilder, root);
        }

        @Override
        protected List<JpaOrderCriteria> toEntityOrderCriteria(EntitySortCriteria.Level sortCriterion) {
            if (sortCriterion.field() instanceof DashboardSearchCriteria.SortableField sortableAttribute) {
                var sortDirection = sortCriterion.order();
                return switch (sortableAttribute) {
                    case NAME -> nullFirstOrderCriteria(dashboardNameLowerCase, dashboardNameNullWeight, sortDirection);
                };
            }
            return emptyList();
        }
    }
}
