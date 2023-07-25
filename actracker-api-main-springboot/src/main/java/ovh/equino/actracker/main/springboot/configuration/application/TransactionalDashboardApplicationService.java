package ovh.equino.actracker.main.springboot.configuration.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ovh.equino.actracker.application.dashboard.DashboardApplicationService;
import ovh.equino.actracker.domain.dashboard.DashboardRepository;
import ovh.equino.actracker.domain.dashboard.DashboardSearchEngine;
import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationEngine;
import ovh.equino.actracker.domain.tenant.TenantRepository;

@Transactional
@Service
class TransactionalDashboardApplicationService extends DashboardApplicationService {

    TransactionalDashboardApplicationService(DashboardRepository dashboardRepository,
                                             DashboardSearchEngine dashboardSearchEngine,
                                             DashboardGenerationEngine dashboardGenerationEngine,
                                             TenantRepository tenantRepository) {

        super(dashboardRepository, dashboardSearchEngine, dashboardGenerationEngine, tenantRepository);
    }
}
