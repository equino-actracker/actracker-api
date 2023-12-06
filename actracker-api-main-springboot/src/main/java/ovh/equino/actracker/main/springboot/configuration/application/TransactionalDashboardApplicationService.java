package ovh.equino.actracker.main.springboot.configuration.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ovh.equino.actracker.application.dashboard.DashboardApplicationService;
import ovh.equino.actracker.domain.dashboard.DashboardDataSource;
import ovh.equino.actracker.domain.dashboard.DashboardNotifier;
import ovh.equino.actracker.domain.dashboard.DashboardRepository;
import ovh.equino.actracker.domain.dashboard.DashboardSearchEngine;
import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationEngine;
import ovh.equino.actracker.domain.tag.TagDataSource;
import ovh.equino.actracker.domain.tenant.TenantDataSource;
import ovh.equino.security.identity.IdentityProvider;

@Transactional
@Service
class TransactionalDashboardApplicationService extends DashboardApplicationService {

    TransactionalDashboardApplicationService(DashboardRepository dashboardRepository,
                                             DashboardDataSource dashboardDataSource,
                                             DashboardSearchEngine dashboardSearchEngine,
                                             DashboardGenerationEngine dashboardGenerationEngine,
                                             DashboardNotifier dashboardNotifier,
                                             TagDataSource tagDataSource,
                                             TenantDataSource tenantDataSource,
                                             IdentityProvider identityProvider) {

        super(
                dashboardRepository,
                dashboardDataSource,
                dashboardSearchEngine,
                dashboardGenerationEngine,
                dashboardNotifier,
                tagDataSource,
                tenantDataSource,
                identityProvider
        );
    }
}
