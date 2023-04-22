package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.UUID;

class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;
    private final DashboardSearchEngine dashboardSearchEngine;

    DashboardServiceImpl(DashboardRepository dashboardRepository, DashboardSearchEngine dashboardSearchEngine) {

        this.dashboardRepository = dashboardRepository;
        this.dashboardSearchEngine = dashboardSearchEngine;
    }

    @Override
    public DashboardDto createDashboard(DashboardDto newDashboardData, User creator) {
        Dashboard dashboard = Dashboard.create(newDashboardData, creator);
        dashboardRepository.add(dashboard.forStorage());
        return dashboard.forClient();
    }

    @Override
    public DashboardDto updateDashboard(UUID dashboardId, DashboardDto updatedDashboardData, User updater) {
        Dashboard dashboard = getDashboardIfAuthorized(updater, dashboardId);
        dashboard.updateTo(updatedDashboardData);
        dashboardRepository.update(dashboardId, dashboard.forStorage());
        return dashboard.forClient();
    }

    @Override
    public EntitySearchResult<DashboardDto> searchDashboards(EntitySearchCriteria searchCriteria) {
        EntitySearchResult<DashboardDto> searchResult = dashboardSearchEngine.findDashboards(searchCriteria);
        List<DashboardDto> resultForClient = searchResult.results().stream()
                .map(Dashboard::fromStorage)
                .map(Dashboard::forClient)
                .toList();

        return new EntitySearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    @Override
    public void deleteDashboard(UUID dashboardId, User remover) {
        Dashboard dashboard = getDashboardIfAuthorized(remover, dashboardId);
        dashboard.delete();
        dashboardRepository.update(dashboardId, dashboard.forStorage());
    }

    private Dashboard getDashboardIfAuthorized(User user, UUID dashboardId) {
        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));

        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);

        if (dashboard.isNotAvailableFor(user)) {
            throw new EntityNotFoundException(Dashboard.class, dashboardId);
        }
        return dashboard;
    }
}
