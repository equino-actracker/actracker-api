package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.user.User;

import java.util.UUID;

public interface DashboardService {

    DashboardDto createDashboard(DashboardDto newDashboardData, User creator);

    DashboardDto updateDashboard(UUID dashboardId, DashboardDto updatedDashboardData, User updater);

    EntitySearchResult<DashboardDto> searchDashboards(EntitySearchCriteria searchCriteria);

    void deleteDashboard(UUID dashboardId, User remover);
}
