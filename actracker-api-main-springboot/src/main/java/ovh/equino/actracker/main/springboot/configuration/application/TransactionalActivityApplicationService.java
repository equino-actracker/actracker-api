package ovh.equino.actracker.main.springboot.configuration.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ovh.equino.actracker.application.activity.ActivityApplicationService;
import ovh.equino.actracker.domain.activity.*;
import ovh.equino.security.identity.IdentityProvider;

@Transactional
@Service
class TransactionalActivityApplicationService extends ActivityApplicationService {

    TransactionalActivityApplicationService(ActivityFactory activityFactory,
                                            ActivityRepository activityRepository,
                                            ActivityDataSource activityDataSource,
                                            ActivitySearchEngine activitySearchEngine,
                                            ActivityNotifier activityNotifier,
                                            IdentityProvider identityProvider) {

        super(
                activityFactory,
                activityRepository,
                activityDataSource,
                activitySearchEngine,
                activityNotifier,
                identityProvider
        );
    }
}
