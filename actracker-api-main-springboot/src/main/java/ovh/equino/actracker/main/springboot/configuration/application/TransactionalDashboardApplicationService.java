package ovh.equino.actracker.main.springboot.configuration.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ovh.equino.actracker.application.dashboard.DashboardApplicationService;
import ovh.equino.actracker.domain.dashboard.DashboardNotifier;
import ovh.equino.actracker.domain.dashboard.DashboardRepository;
import ovh.equino.actracker.domain.dashboard.DashboardSearchEngine;
import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationEngine;
import ovh.equino.actracker.domain.tenant.TenantRepository;
import ovh.equino.security.identity.IdentityProvider;

@Transactional
@Service
class TransactionalDashboardApplicationService extends DashboardApplicationService {

    TransactionalDashboardApplicationService(DashboardRepository dashboardRepository,
                                             DashboardSearchEngine dashboardSearchEngine,
                                             DashboardGenerationEngine dashboardGenerationEngine,
                                             DashboardNotifier dashboardNotifier,
                                             TenantRepository tenantRepository,
                                             IdentityProvider identityProvider) {

        super(
                dashboardRepository,
                dashboardSearchEngine,
                dashboardGenerationEngine,
                dashboardNotifier,
                tenantRepository,
                identityProvider
        );
    }
}
