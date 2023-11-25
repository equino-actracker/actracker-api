package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.EntitySearchCriteria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DashboardRepository {

    void add(DashboardDto dashboard);

    void update(UUID dashboardId, DashboardDto dashboard);

    // TODO delete when data sources proven
    Optional<DashboardDto> findById(UUID dashboardId);

    // TODO delete when data sources proven
    List<DashboardDto> find(EntitySearchCriteria searchCriteria);
}
