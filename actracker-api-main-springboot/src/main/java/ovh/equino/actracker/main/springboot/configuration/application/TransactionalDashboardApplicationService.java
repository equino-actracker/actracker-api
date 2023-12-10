package ovh.equino.actracker.main.springboot.configuration.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ovh.equino.actracker.application.dashboard.DashboardApplicationService;
import ovh.equino.actracker.domain.dashboard.*;
import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationEngine;
import ovh.equino.actracker.domain.tenant.TenantDataSource;
import ovh.equino.security.identity.IdentityProvider;

@Transactional
@Service
class TransactionalDashboardApplicationService extends DashboardApplicationService {

    TransactionalDashboardApplicationService(DashboardFactory dashboardFactory,
                                             DashboardRepository dashboardRepository,
                                             DashboardDataSource dashboardDataSource,
                                             DashboardSearchEngine dashboardSearchEngine,
                                             DashboardGenerationEngine dashboardGenerationEngine,
                                             DashboardNotifier dashboardNotifier,
                                             TenantDataSource tenantDataSource,
                                             IdentityProvider identityProvider) {

        super(
                dashboardFactory,
                dashboardRepository,
                dashboardDataSource,
                dashboardSearchEngine,
                dashboardGenerationEngine,
                dashboardNotifier,
                tenantDataSource,
                identityProvider
        );
    }
}
