package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.dashboard.DashboardDataSource;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

class JpaDashboardDataSource extends JpaDAO implements DashboardDataSource {

    JpaDashboardDataSource(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public Optional<DashboardDto> find(UUID dashboardId) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public List<DashboardDto> find(EntitySearchCriteria searchCriteria) {
        throw new RuntimeException("Not implemented yet");
    }
}
