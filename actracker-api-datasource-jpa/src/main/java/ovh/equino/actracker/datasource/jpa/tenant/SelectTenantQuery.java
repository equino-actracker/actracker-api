package ovh.equino.actracker.datasource.jpa.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.JpaSortCriteria;
import ovh.equino.actracker.datasource.jpa.SingleResultJpaQuery;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.jpa.tenant.TenantEntity;
import ovh.equino.actracker.jpa.tenant.TenantEntity_;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

final class SelectTenantQuery extends SingleResultJpaQuery<TenantEntity, TenantProjection> {

    private final PredicateBuilder predicate;

    SelectTenantQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicate = new PredicateBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
                criteriaBuilder.construct(
                        TenantProjection.class,
                        root.get(TenantEntity_.id),
                        root.get(TenantEntity_.username),
                        root.get(TenantEntity_.password)
                )
        );
    }

    @Override
    public PredicateBuilder predicate() {
        return predicate;
    }

    @Override
    public SelectTenantQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    @Override
    protected Class<TenantEntity> getRootEntityType() {
        return TenantEntity.class;
    }

    @Override
    protected Class<TenantProjection> getProjectionType() {
        return TenantProjection.class;
    }

    final class PredicateBuilder extends JpaPredicateBuilder<TenantEntity> {
        private PredicateBuilder() {
            super(criteriaBuilder, root);
        }

        JpaPredicate hasUsername(String username) {
            return () -> criteriaBuilder.equal(root.get(TenantEntity_.username), username);
        }

        @Override
        protected List<JpaSortCriteria> toEntityOrderCriteria(EntitySortCriteria.Level sortCriterion) {
            return emptyList();
        }

        @Override
        protected List<PageCondition<? extends Comparable<?>>> toEntityPageConditions(
                EntitySearchPageId.Value pageAttribute) {

            return emptyList();
        }
    }
}
