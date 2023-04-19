package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.domain.user.User;

import java.util.UUID;

class TagSetServiceImpl implements TagSetService {

    private final TagSetRepository tagSetRepository;
    private final TagSetSearchEngine tagSetSearchEngine;
    private final TagRepository tagRepository;

    TagSetServiceImpl(TagSetRepository tagSetRepository,
                      TagSetSearchEngine tagSetSearchEngine,
                      TagRepository tagRepository) {

        this.tagSetRepository = tagSetRepository;
        this.tagSetSearchEngine = tagSetSearchEngine;
        this.tagRepository = tagRepository;
    }

    @Override
    public TagSetDto createTagSet(TagSetDto newTagSetData, User creator) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public TagSetDto updateTagSet(UUID tagSetId, TagSetDto updatedTagSetData, User updater) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public EntitySearchResult<TagSetDto> searchTagSets(EntitySearchCriteria searchCriteria) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public void deleteTagSet(UUID tagSetId, User remover) {
        throw new IllegalStateException("Not implemented yet");
    }
}
