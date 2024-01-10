package ovh.equino.actracker.main.springboot.configuration.application;

import org.springframework.stereotype.Service;
import ovh.equino.actracker.application.tag.TagApplicationService;
import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.tenant.TenantDataSource;
import ovh.equino.actracker.domain.user.ActorExtractor;

//@Transactional
@Service
class TransactionalTagApplicationService extends TagApplicationService {

    TransactionalTagApplicationService(TagFactory tagFactory,
                                       MetricFactory metricFactory,
                                       TagRepository tagRepository,
                                       TagDataSource tagDataSource,
                                       TagSearchEngine tagSearchEngine,
                                       TagNotifier tagNotifier,
                                       ActorExtractor actorExtractor,
                                       TenantDataSource tenantDataSource) {

        super(
                tagFactory,
                metricFactory,
                tagRepository,
                tagDataSource,
                tagSearchEngine,
                tagNotifier,
                actorExtractor,
                tenantDataSource
        );
    }
}
