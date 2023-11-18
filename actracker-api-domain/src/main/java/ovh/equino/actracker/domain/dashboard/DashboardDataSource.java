package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.EntitySearchCriteria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DashboardDataSource {

    Optional<DashboardDto> find(UUID dashboardId);

    List<DashboardDto> find(EntitySearchCriteria searchCriteria);
}
