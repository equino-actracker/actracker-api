package ovh.equino.actracker.datasource.jpa.dashboard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import ovh.equino.actracker.jpa.dashboard.ChartEntity;
import ovh.equino.actracker.jpa.dashboard.ChartEntity_;
import ovh.equino.actracker.jpa.dashboard.DashboardEntity;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.JpaSortBuilder;
import ovh.equino.actracker.datasource.jpa.MultiResultJpaQuery;
import ovh.equino.actracker.jpa.dashboard.DashboardEntity_;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static jakarta.persistence.criteria.JoinType.INNER;
import static java.util.stream.Collectors.toUnmodifiableSet;

final class SelectChartJoinDashboardQuery extends MultiResultJpaQuery<ChartEntity, ChartJoinDashboardProjection> {

    private final PredicateBuilder predicate;
    private final Join<ChartEntity, DashboardEntity> dashboard;

    SelectChartJoinDashboardQuery(EntityManager entityManager) {
        super(entityManager);
        dashboard = root.join(ChartEntity_.dashboard, INNER);
        this.predicate = new PredicateBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
                criteriaBuilder.construct(
                        ChartJoinDashboardProjection.class,
                        root.get(ChartEntity_.id),
                        dashboard.get(DashboardEntity_.id),
                        root.get(ChartEntity_.name),
                        root.get(ChartEntity_.groupBy),
                        root.get(ChartEntity_.metric),
                        root.get(ChartEntity_.deleted)
                )
        );
    }

    @Override
    public PredicateBuilder predicate() {
        return predicate;
    }

    @Override
    public SelectChartJoinDashboardQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    /**
     * @deprecated Sorting this entity is not supported. An attempt will throw RuntimeException.
     */
    @Override
    @Deprecated
    public JpaSortBuilder<ChartEntity> sort() {
        throw new RuntimeException("Sorting charts joint with dashboards not supported");
    }

    @Override
    protected Class<ChartEntity> getRootEntityType() {
        return ChartEntity.class;
    }

    @Override
    protected Class<ChartJoinDashboardProjection> getProjectionType() {
        return ChartJoinDashboardProjection.class;
    }

    public final class PredicateBuilder extends JpaPredicateBuilder<ChartEntity> {
        private PredicateBuilder() {
            super(criteriaBuilder, root);
        }

        public JpaPredicate isNotDeleted() {
            return () -> criteriaBuilder.isFalse(root.get(ChartEntity_.deleted));
        }

        public JpaPredicate hasDashboardId(UUID dashboardId) {
            return () -> criteriaBuilder.equal(dashboard.get(DashboardEntity_.id), dashboardId.toString());
        }

        public JpaPredicate hasDashboardIdIn(Collection<UUID> dashboardIds) {
            Set<String> dashboardIdsAsString = dashboardIds
                    .stream()
                    .map(UUID::toString)
                    .collect(toUnmodifiableSet());
            return in(dashboardIdsAsString, dashboard.get(DashboardEntity_.id));
        }
    }
}
