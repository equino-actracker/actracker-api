package ovh.equino.actracker.application.dashboard;

import ovh.equino.actracker.domain.dashboard.Dashboard;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.DashboardRepository;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tenant.TenantRepository;
import ovh.equino.actracker.domain.user.User;

import java.util.UUID;

public class DashboardApplicationService {

    private final DashboardRepository dashboardRepository;
    private final TenantRepository tenantRepository;

    DashboardApplicationService(DashboardRepository dashboardRepository, TenantRepository tenantRepository) {
        this.dashboardRepository = dashboardRepository;
        this.tenantRepository = tenantRepository;
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

    public DashboardDto shareDashboard(Share newShare, UUID dashboardId, User granter) {
        DashboardDto dashboardDto = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));
        Dashboard dashboard = Dashboard.fromStorage(dashboardDto);

        Share share = tenantRepository.findByUsername(newShare.granteeName())
                .map(tenant -> new Share(
                        new User(tenant.id()),
                        tenant.username()
                ))
                .orElse(new Share(newShare.granteeName()));

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
}
