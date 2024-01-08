package ovh.equino.actracker.main.springboot.configuration.application;

import org.springframework.stereotype.Service;
import ovh.equino.actracker.application.tagset.TagSetApplicationService;
import ovh.equino.actracker.domain.tagset.*;
import ovh.equino.actracker.domain.user.ActorExtractor;

//@Transactional
@Service
class TransactionalTagSetApplicationService extends TagSetApplicationService {

    TransactionalTagSetApplicationService(TagSetFactory tagSetFactory,
                                          TagSetRepository tagSetRepository,
                                          TagSetDataSource tagSetDataSource,
                                          TagSetSearchEngine tagSetSearchEngine,
                                          TagSetNotifier tagSetNotifier,
                                          ActorExtractor actorExtractor) {

        super(tagSetFactory, tagSetRepository, tagSetDataSource, tagSetSearchEngine, tagSetNotifier, actorExtractor);
    }
}
