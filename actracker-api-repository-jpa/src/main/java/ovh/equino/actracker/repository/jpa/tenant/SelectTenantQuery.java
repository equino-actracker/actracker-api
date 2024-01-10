package ovh.equino.actracker.repository.jpa.tenant;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.jpa.tenant.TenantEntity;
import ovh.equino.actracker.repository.jpa.JpaPredicate;
import ovh.equino.actracker.repository.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.repository.jpa.SingleResultJpaQuery;

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
                        root.get("id"),
                        root.get("username"),
                        root.get("password")
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
            return () -> criteriaBuilder.equal(root.get("username"), username);
        }
    }
}
