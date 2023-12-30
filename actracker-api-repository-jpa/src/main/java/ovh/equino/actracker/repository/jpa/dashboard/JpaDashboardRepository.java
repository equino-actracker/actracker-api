package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import ovh.equino.actracker.domain.dashboard.*;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.Optional;
import java.util.UUID;

class JpaDashboardRepository extends JpaDAO implements DashboardRepository {

    private final DashboardMapper dashboardMapper;

    JpaDashboardRepository(EntityManager entityManager, DashboardFactory dashboardFactory) {
        super(entityManager);
        this.dashboardMapper = new DashboardMapper(dashboardFactory);
    }

    @Override
    public void add(DashboardDto dashboard) {
        DashboardEntity dashboardEntity = dashboardMapper.toEntity(dashboard);
        entityManager.persist(dashboardEntity);
    }

    @Override
    public void update(UUID dashboardId, DashboardDto dashboard) {
        DashboardEntity dashboardEntity = dashboardMapper.toEntity(dashboard);
        dashboardEntity.id = dashboardId.toString();
        entityManager.merge(dashboardEntity);
    }

    @Override
    public Optional<DashboardDto> findById(UUID dashboardId) {
        DashboardQueryBuilder queryBuilder = new DashboardQueryBuilder(entityManager);

        CriteriaQuery<DashboardEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.hasId(dashboardId),
                                queryBuilder.isNotDeleted()
                        )
                );

        TypedQuery<DashboardEntity> typedQuery = entityManager.createQuery(query);

        return typedQuery.getResultList().stream()
                .findFirst()
                .map(dashboardMapper::toDto);
    }

    @Override
    public Optional<Dashboard> get(DashboardId dashboardId) {
        DashboardEntity entity = entityManager.find(DashboardEntity.class, dashboardId.id().toString());
        Dashboard dashboard = dashboardMapper.toDomainObject(entity);
        return Optional.ofNullable(dashboard);
    }

    @Override
    public void add(Dashboard dashboard) {
        DashboardDto dto = dashboard.forStorage();
        DashboardEntity entity = dashboardMapper.toEntity(dto);
        entityManager.persist(entity);
    }

    @Override
    public void save(Dashboard dashboard) {
        DashboardDto dto = dashboard.forStorage();
        DashboardEntity entity = dashboardMapper.toEntity(dto);
        entityManager.merge(entity);
    }
}
