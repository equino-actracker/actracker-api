package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationEngine;
import ovh.equino.actracker.domain.tenant.TenantRepository;

class DashboardServiceImpl {

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







}
