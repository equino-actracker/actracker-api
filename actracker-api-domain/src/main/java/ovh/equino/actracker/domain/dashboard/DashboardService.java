package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.dashboard.generation.DashboardData;
import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationCriteria;
import ovh.equino.actracker.domain.user.User;

import java.util.UUID;

public interface DashboardService {

    DashboardDto getDashboard(UUID dashboardId, User searcher);

    DashboardDto createDashboard(DashboardDto newDashboardData, User creator);

    DashboardDto updateDashboard(UUID dashboardId, DashboardDto updatedDashboardData, User updater);

    EntitySearchResult<DashboardDto> searchDashboards(EntitySearchCriteria searchCriteria);

    void deleteDashboard(UUID dashboardId, User remover);

    DashboardData generateDashboard(DashboardGenerationCriteria generationCriteria);

    DashboardDto shareDashboard(UUID dashboardId, String granteeName, User granter);
}
