package ovh.equino.actracker.domain.dashboard;

import java.util.Optional;
import java.util.UUID;

public interface DashboardRepository {

    void add(DashboardDto dashboard);

    void update(UUID dashboardId, DashboardDto dashboard);

    // TODO delete when data sources proven
    Optional<DashboardDto> findById(UUID dashboardId);
}
