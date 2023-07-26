package ovh.equino.actracker.application.dashboard;

import ovh.equino.actracker.application.SearchResult;
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
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import java.util.List;
import java.util.UUID;

public class DashboardApplicationService {

    private final DashboardRepository dashboardRepository;
    private final DashboardSearchEngine dashboardSearchEngine;
    private final DashboardGenerationEngine dashboardGenerationEngine;
    private final TenantRepository tenantRepository;
    private final IdentityProvider identityProvider;

    public DashboardApplicationService(DashboardRepository dashboardRepository,
                                       DashboardSearchEngine dashboardSearchEngine,
                                       DashboardGenerationEngine dashboardGenerationEngine,
                                       TenantRepository tenantRepository,
                                       IdentityProvider identityProvider) {

        this.dashboardRepository = dashboardRepository;
        this.dashboardSearchEngine = dashboardSearchEngine;
        this.dashboardGenerationEngine = dashboardGenerationEngine;
        this.tenantRepository = tenantRepository;
        this.identityProvider = identityProvider;
    }

    public DashboardResult getDashboard(UUID dashboardId) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User searcher = new User(requestIdentity.getId());

        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));

        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);
        DashboardDto dashboardResult = dashboard.forClient(searcher);

        return toDashboardResult(dashboardResult);
    }

    public DashboardResult createDashboard(DashboardDto newDashboardData) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User creator = new User(requestIdentity.getId());

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
        DashboardDto dashboardResult = dashboard.forClient(creator);

        return toDashboardResult(dashboardResult);
    }

    public SearchResult<DashboardResult> searchDashboards(SearchDashboardsQuery searchDashboardsQuery) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User searcher = new User(requestIdentity.getId());

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                searchDashboardsQuery.pageSize(),
                searchDashboardsQuery.pageId(),
                searchDashboardsQuery.term(),
                null,
                null,
                searchDashboardsQuery.excludeFilter(),
                null,
                null
        );

        EntitySearchResult<DashboardDto> searchResult = dashboardSearchEngine.findDashboards(searchCriteria);
        List<DashboardResult> resultForClient = searchResult.results().stream()
                .map(Dashboard::fromStorage)
                .map(dashboard -> dashboard.forClient(searchCriteria.searcher()))
                .map(this::toDashboardResult)
                .toList();

        return new SearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    public DashboardResult renameDashboard(String newName, UUID dashboardId) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User updater = new User(requestIdentity.getId());

        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));
        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);

        dashboard.rename(newName, updater);
        dashboardRepository.update(dashboardId, dashboard.forStorage());
        DashboardDto dashboardResult = dashboard.forClient(updater);

        return toDashboardResult(dashboardResult);
    }

    public void deleteDashboard(UUID dashboardId) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User remover = new User(requestIdentity.getId());

        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));
        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);

        dashboard.delete(remover);
        dashboardRepository.update(dashboardId, dashboard.forStorage());
    }

    public DashboardResult addChart(Chart newChart, UUID dashboardId) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User updater = new User(requestIdentity.getId());

        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));
        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);

        dashboard.addChart(newChart, updater);
        dashboardRepository.update(dashboardId, dashboard.forStorage());
        DashboardDto dashboardResult = dashboard.forClient(updater);
        return toDashboardResult(dashboardResult);
    }

    public DashboardResult deleteChart(UUID chartId, UUID dashboardId) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User updater = new User(requestIdentity.getId());

        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));
        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);

        dashboard.deleteChart(new ChartId(chartId), updater);
        dashboardRepository.update(dashboardId, dashboard.forStorage());
        DashboardDto dashboardResult = dashboard.forClient(updater);
        return toDashboardResult(dashboardResult);

    }

    public DashboardResult shareDashboard(Share newShare, UUID dashboardId) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User granter = new User(requestIdentity.getId());

        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));
        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);

        Share share = resolveShare(newShare);

        dashboard.share(share, granter);
        dashboardRepository.update(dashboardId, dashboard.forStorage());
        DashboardDto dashboardResult = dashboard.forClient(granter);

        return toDashboardResult(dashboardResult);
    }

    public DashboardResult unshareDashboard(String granteeName, UUID dashboardId) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User granter = new User(requestIdentity.getId());

        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));
        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);

        dashboard.unshare(granteeName, granter);
        dashboardRepository.update(dashboardId, dashboard.forStorage());
        DashboardDto dashboardResult = dashboard.forClient(granter);

        return toDashboardResult(dashboardResult);
    }

    public DashboardData generateDashboard(GenerateDashboardQuery generateDashboardQuery) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User generator = new User(requestIdentity.getId());

        DashboardGenerationCriteria generationCriteria = new DashboardGenerationCriteria(
                generateDashboardQuery.dashboardId(),
                generator,
                generateDashboardQuery.timeRangeStart(),
                generateDashboardQuery.timeRangeEnd(),
                generateDashboardQuery.tags()
        );

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

    DashboardResult toDashboardResult(DashboardDto dashboardDto) {
        List<ChartResult> chartResults = dashboardDto.charts().stream()
                .map(this::toChartResult)
                .toList();
        List<String> shares = dashboardDto.shares().stream()
                .map(Share::granteeName)
                .toList();
        return new DashboardResult(
                dashboardDto.id(),
                dashboardDto.name(),
                chartResults,
                shares
        );
    }

    ChartResult toChartResult(Chart chart) {
        return new ChartResult(
                chart.id().id(),
                chart.name(),
                chart.groupBy().toString(),
                chart.analysisMetric().toString(),
                chart.includedTags()
        );
    }
}
