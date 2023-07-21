package ovh.equino.actracker.main.springboot.configuration.application;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ovh.equino.actracker.application.tag.TagApplicationService;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.domain.tag.TagSearchEngine;
import ovh.equino.actracker.domain.tenant.TenantRepository;
import ovh.equino.security.identity.IdentityProvider;

@Transactional
@Service
class TransactionalTagApplicationService extends TagApplicationService {

    TransactionalTagApplicationService(TagRepository tagRepository,
                                       TagSearchEngine tagSearchEngine,
                                       IdentityProvider identityProvider,
                                       TenantRepository tenantRepository) {

        super(tagRepository, tagSearchEngine, identityProvider, tenantRepository);
    }
}
