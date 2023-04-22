package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.DashboardRepository;
import ovh.equino.actracker.repository.jpa.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

class JpaDashboardRepository extends JpaRepository implements DashboardRepository {

    @Override
    public void add(DashboardDto dashboard) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public void update(UUID dashboardId, DashboardDto dashboard) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public Optional<DashboardDto> findById(UUID dashboardId) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public List<DashboardDto> find(EntitySearchCriteria searchCriteria) {
        throw new IllegalStateException("Not implemented yet");
    }
}
