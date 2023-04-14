package ovh.equino.actracker.search.repository.tag;

import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.tag.TagSearchEngine;
import ovh.equino.actracker.domain.EntitySearchResult;

class RepositoryTagSearchEngine implements TagSearchEngine {

    private final TagRepository tagRepository;

    RepositoryTagSearchEngine(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public EntitySearchResult<TagDto> findTags(EntitySearchCriteria searchCriteria) {
        return tagRepository.find(searchCriteria);
    }
}
