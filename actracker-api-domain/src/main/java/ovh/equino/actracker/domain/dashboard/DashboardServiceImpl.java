package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationEngine;
import ovh.equino.actracker.domain.tenant.TenantDataSource;

// TODO delete me, unused
class DashboardServiceImpl {

    private final DashboardRepository dashboardRepository;
    private final DashboardSearchEngine dashboardSearchEngine;
    private final DashboardGenerationEngine dashboardGenerationEngine;
    private final TenantDataSource tenantRepository;

    DashboardServiceImpl(DashboardRepository dashboardRepository,
                         DashboardSearchEngine dashboardSearchEngine,
                         DashboardGenerationEngine dashboardGenerationEngine,
                         TenantDataSource tenantRepository) {

        this.dashboardRepository = dashboardRepository;
        this.dashboardSearchEngine = dashboardSearchEngine;
        this.dashboardGenerationEngine = dashboardGenerationEngine;
        this.tenantRepository = tenantRepository;
    }







}
