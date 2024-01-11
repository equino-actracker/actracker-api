package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.dashboard.*;
import ovh.equino.actracker.jpa.dashboard.DashboardEntity;
import ovh.equino.actracker.jpa.JpaDAO;

import java.util.Optional;

import static java.util.Objects.nonNull;

class JpaDashboardRepository extends JpaDAO implements DashboardRepository {

    private final DashboardMapper dashboardMapper;

    JpaDashboardRepository(EntityManager entityManager, DashboardFactory dashboardFactory) {
        super(entityManager);
        this.dashboardMapper = new DashboardMapper(dashboardFactory);
    }

    @Override
    public Optional<Dashboard> get(DashboardId dashboardId) {
        DashboardEntity entity = entityManager.find(DashboardEntity.class, dashboardId.id().toString());
        Dashboard dashboard = dashboardMapper.toDomainObject(entity);
        if (nonNull(entity)) {
            entityManager.detach(entity);
        }
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
