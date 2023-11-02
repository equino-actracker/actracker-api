package ovh.equino.actracker.repository.jpa.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.tenant.TenantRepository;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.Optional;

class JpaTenantRepository extends JpaDAO implements TenantRepository {

    JpaTenantRepository(EntityManager entityManager) {
        super(entityManager);
    }

    private final TenantMapper tenantMapper = new TenantMapper();

    @Override
    public Optional<TenantDto> findByUsername(String username) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TenantEntity> criteriaQuery = criteriaBuilder.createQuery(TenantEntity.class);
        Root<TenantEntity> rootEntity = criteriaQuery.from(TenantEntity.class);
        CriteriaQuery<TenantEntity> query = criteriaQuery
                .select(rootEntity)
                .where(criteriaBuilder.equal(rootEntity.get("username"), username));
        TypedQuery<TenantEntity> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultStream()
                .findFirst()
                .map(tenantMapper::toDto);
    }
}
