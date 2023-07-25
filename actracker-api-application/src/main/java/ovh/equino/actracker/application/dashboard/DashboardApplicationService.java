package ovh.equino.actracker.application.dashboard;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.dashboard.*;
import ovh.equino.actracker.domain.dashboard.generation.DashboardData;
import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationCriteria;
import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationEngine;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tenant.TenantRepository;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.UUID;

public class DashboardApplicationService {

    private final DashboardRepository dashboardRepository;
    private final DashboardSearchEngine dashboardSearchEngine;
    private final DashboardGenerationEngine dashboardGenerationEngine;
    private final TenantRepository tenantRepository;

    public DashboardApplicationService(DashboardRepository dashboardRepository,
                                       DashboardSearchEngine dashboardSearchEngine,
                                       DashboardGenerationEngine dashboardGenerationEngine,
                                       TenantRepository tenantRepository) {

        this.dashboardRepository = dashboardRepository;
        this.dashboardSearchEngine = dashboardSearchEngine;
        this.dashboardGenerationEngine = dashboardGenerationEngine;
        this.tenantRepository = tenantRepository;
    }

    public DashboardDto getDashboard(UUID dashboardId, User searcher) {
        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));

        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);
        return dashboard.forClient(searcher);
    }

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

    public EntitySearchResult<DashboardDto> searchDashboards(EntitySearchCriteria searchCriteria) {
        EntitySearchResult<DashboardDto> searchResult = dashboardSearchEngine.findDashboards(searchCriteria);
        List<DashboardDto> resultForClient = searchResult.results().stream()
                .map(Dashboard::fromStorage)
                .map(dashboard -> dashboard.forClient(searchCriteria.searcher()))
                .toList();

        return new EntitySearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    public DashboardDto renameDashboard(String newName, UUID dashboardId, User updater) {
        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));
        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);

        dashboard.rename(newName, updater);
        dashboardRepository.update(dashboardId, dashboard.forStorage());
        return dashboard.forClient(updater);
    }

    public void deleteDashboard(UUID dashboardId, User remover) {
        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));
        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);

        dashboard.delete(remover);
        dashboardRepository.update(dashboardId, dashboard.forStorage());
    }

    public DashboardDto addChart(Chart newChart, UUID dashboardId, User editor) {
        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));
        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);

        dashboard.addChart(newChart, editor);
        dashboardRepository.update(dashboardId, dashboard.forStorage());
        return dashboard.forClient(editor);
    }

    public DashboardDto deleteChart(UUID chartId, UUID dashboardId, User editor) {
        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));
        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);

        dashboard.deleteChart(new ChartId(chartId), editor);
        dashboardRepository.update(dashboardId, dashboard.forStorage());
        return dashboard.forClient(editor);

    }

    public DashboardDto shareDashboard(Share newShare, UUID dashboardId, User granter) {
        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));
        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);

        Share share = resolveShare(newShare);

        dashboard.share(share, granter);
        dashboardRepository.update(dashboardId, dashboard.forStorage());
        return dashboard.forClient(granter);
    }

    public DashboardDto unshareDashboard(String granteeName, UUID dashboardId, User granter) {
        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));
        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);

        dashboard.unshare(granteeName, granter);
        dashboardRepository.update(dashboardId, dashboard.forStorage());
        return dashboard.forClient(granter);
    }

    public DashboardData generateDashboard(DashboardGenerationCriteria generationCriteria) {
        UUID dashboardId = generationCriteria.dashboardId();
        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));

        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);
        return dashboardGenerationEngine.generateDashboard(dashboard.forStorage(), generationCriteria);
    }


    private Share resolveShare(Share share) {
        return tenantRepository.findByUsername(share.granteeName())
                .map(tenant -> new Share(
                        new User(tenant.id()),
                        tenant.username()
                ))
                .orElse(new Share(share.granteeName()));
    }
}
