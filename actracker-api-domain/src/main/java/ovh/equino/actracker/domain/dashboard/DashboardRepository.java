package ovh.equino.actracker.domain.dashboard;

import java.util.Optional;

public interface DashboardRepository {

    Optional<Dashboard> get(DashboardId dashboardId);

    void add(Dashboard dashboard);

    // TODO remove, replace with domain events
    void save(Dashboard dashboard);
}
