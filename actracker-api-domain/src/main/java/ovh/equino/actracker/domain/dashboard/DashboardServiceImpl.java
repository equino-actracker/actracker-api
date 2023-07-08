package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.dashboard.generation.DashboardData;
import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationCriteria;
import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationEngine;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tenant.TenantRepository;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.UUID;

class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;
    private final DashboardSearchEngine dashboardSearchEngine;
    private final DashboardGenerationEngine dashboardGenerationEngine;
    private final TenantRepository tenantRepository;

    DashboardServiceImpl(DashboardRepository dashboardRepository,
                         DashboardSearchEngine dashboardSearchEngine,
                         DashboardGenerationEngine dashboardGenerationEngine,
                         TenantRepository tenantRepository) {

        this.dashboardRepository = dashboardRepository;
        this.dashboardSearchEngine = dashboardSearchEngine;
        this.dashboardGenerationEngine = dashboardGenerationEngine;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public DashboardDto getDashboard(UUID dashboardId, User searcher) {
        Dashboard dashboard = getDashboardIfAuthorized(searcher, dashboardId);
        return dashboard.forClient(searcher);
    }

    @Override
    public DashboardDto createDashboard(DashboardDto newDashboardData, User creator) {
        DashboardDto dashboardDataWithSharesResolved = new DashboardDto(
                newDashboardData.id(),
                newDashboardData.creatorId(),
                newDashboardData.name(),
                newDashboardData.charts(),
                newDashboardData.shares().stream()
                        .map(this::resolveShare)
                        .toList(),
                newDashboardData.deleted()
        );
        Dashboard dashboard = Dashboard.create(dashboardDataWithSharesResolved, creator);
        dashboardRepository.add(dashboard.forStorage());
        return dashboard.forClient(creator);
    }

    @Override
    public DashboardDto updateDashboard(UUID dashboardId, DashboardDto updatedDashboardData, User updater) {
        Dashboard dashboard = getDashboardIfAuthorized(updater, dashboardId);
        dashboard.updateTo(updatedDashboardData, updater);
        dashboardRepository.update(dashboardId, dashboard.forStorage());
        return dashboard.forClient(updater);
    }

    @Override
    public EntitySearchResult<DashboardDto> searchDashboards(EntitySearchCriteria searchCriteria) {
        EntitySearchResult<DashboardDto> searchResult = dashboardSearchEngine.findDashboards(searchCriteria);
        List<DashboardDto> resultForClient = searchResult.results().stream()
                .map(Dashboard::fromStorage)
                .map(dashboard -> dashboard.forClient(searchCriteria.searcher()))
                .toList();

        return new EntitySearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    @Override
    public void deleteDashboard(UUID dashboardId, User remover) {
        Dashboard dashboard = getDashboardIfAuthorized(remover, dashboardId);
        dashboard.delete(remover);
        dashboardRepository.update(dashboardId, dashboard.forStorage());
    }

    @Override
    public DashboardData generateDashboard(DashboardGenerationCriteria generationCriteria) {
        UUID dashboardId = generationCriteria.dashboardId();
        Dashboard dashboard = getDashboardIfAuthorized(generationCriteria.generator(), dashboardId);
        return dashboardGenerationEngine.generateDashboard(dashboard.forStorage(), generationCriteria);
    }

    @Override
    public DashboardDto shareDashboard(UUID dashboardId, Share share, User granter) {
        Dashboard dashboard = getDashboardIfAuthorized(granter, dashboardId);
        Share newShare = resolveShare(share);

        dashboard.share(newShare, granter);
        dashboardRepository.update(dashboardId, dashboard.forStorage());
        return dashboard.forClient(granter);
    }

    private Share resolveShare(Share share) {
        return tenantRepository.findByUsername(share.granteeName())
                .map(tenant -> new Share(
                        new User(tenant.id()),
                        tenant.username()
                ))
                .orElse(new Share(share.granteeName()));
    }

    private Dashboard getDashboardIfAuthorized(User user, UUID dashboardId) {
        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));

        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);

        if (dashboard.isNotAccessibleFor(user)) {
            throw new EntityNotFoundException(Dashboard.class, dashboardId);
        }
        return dashboard;
    }
}
