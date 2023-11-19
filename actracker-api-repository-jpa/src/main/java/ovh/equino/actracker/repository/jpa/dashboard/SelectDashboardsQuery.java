package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.repository.jpa.JpaPredicate;
import ovh.equino.actracker.repository.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.repository.jpa.JpaSortBuilder;
import ovh.equino.actracker.repository.jpa.MultiResultJpaQuery;

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
    }

    public final class SortBuilder extends JpaSortBuilder<DashboardEntity> {
        private SortBuilder() {
            super(criteriaBuilder, root);
        }
    }
}
