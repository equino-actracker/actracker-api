package ovh.equino.actracker.main.springboot.configuration.application;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.application.tagset.CreateTagSetCommand;
import ovh.equino.actracker.application.tagset.SearchTagSetsQuery;
import ovh.equino.actracker.application.tagset.TagSetApplicationService;
import ovh.equino.actracker.application.tagset.TagSetResult;
import ovh.equino.actracker.domain.tagset.*;
import ovh.equino.security.identity.IdentityProvider;

import java.util.UUID;

@Transactional
@Service
class TransactionalTagSetApplicationService extends TagSetApplicationService {

    TransactionalTagSetApplicationService(TagSetFactory tagSetFactory,
                                          TagSetRepository tagSetRepository,
                                          TagSetDataSource tagSetDataSource,
                                          TagSetSearchEngine tagSetSearchEngine,
                                          TagSetNotifier tagSetNotifier,
                                          IdentityProvider identityProvider) {

        super(tagSetFactory, tagSetRepository, tagSetDataSource, tagSetSearchEngine, tagSetNotifier, identityProvider);
    }

    @Override
    public TagSetResult createTagSet(CreateTagSetCommand createTagSetCommand) {
        return super.createTagSet(createTagSetCommand);
    }

    @Override
    public SearchResult<TagSetResult> searchTagSets(SearchTagSetsQuery searchTagSetsQuery) {
        return super.searchTagSets(searchTagSetsQuery);
    }

    @Override
    public TagSetResult renameTagSet(String newName, UUID tagSetId) {
        return super.renameTagSet(newName, tagSetId);
    }

    @Override
    public TagSetResult addTagToSet(UUID tagId, UUID tagSetId) {
        return super.addTagToSet(tagId, tagSetId);
    }

    @Override
    public TagSetResult removeTagFromSet(UUID tagId, UUID tagSetId) {
        return super.removeTagFromSet(tagId, tagSetId);
    }

    @Override
    public void deleteTagSet(UUID tagSetId) {
        super.deleteTagSet(tagSetId);
    }
}
