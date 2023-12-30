package ovh.equino.actracker.domain.dashboard;

import java.util.Optional;
import java.util.UUID;

public interface DashboardRepository {

    // TODO remove
    void add(DashboardDto dashboard);

    // TODO remove
    void update(UUID dashboardId, DashboardDto dashboard);

    // TODO remove
    Optional<DashboardDto> findById(UUID dashboardId);

    Optional<Dashboard> get(DashboardId dashboardId);

    void add(Dashboard dashboard);

    // TODO remove, replace with domain events
    void save(Dashboard dashboard);
}
