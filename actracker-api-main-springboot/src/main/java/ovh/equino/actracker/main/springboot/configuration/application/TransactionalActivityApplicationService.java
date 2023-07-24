package ovh.equino.actracker.main.springboot.configuration.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ovh.equino.actracker.application.activity.ActivityApplicationService;
import ovh.equino.actracker.domain.activity.ActivityRepository;
import ovh.equino.actracker.domain.activity.ActivitySearchEngine;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.security.identity.IdentityProvider;

@Transactional
@Service
class TransactionalActivityApplicationService extends ActivityApplicationService {

    TransactionalActivityApplicationService(ActivityRepository activityRepository,
                                            ActivitySearchEngine activitySearchEngine,
                                            TagRepository tagRepository,
                                            IdentityProvider identityProvider) {

        super(activityRepository, activitySearchEngine, tagRepository, identityProvider);
    }
}
