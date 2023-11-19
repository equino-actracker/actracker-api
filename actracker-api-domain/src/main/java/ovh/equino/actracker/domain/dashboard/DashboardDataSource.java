package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Optional;

public interface DashboardDataSource {

    Optional<DashboardDto> find(DashboardId dashboardId, User searcher);

    List<DashboardDto> find(EntitySearchCriteria searchCriteria);
}
