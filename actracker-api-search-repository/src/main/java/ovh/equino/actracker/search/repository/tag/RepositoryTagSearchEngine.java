package ovh.equino.actracker.search.repository.tag;

import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;
import ovh.equino.actracker.domain.tag.TagSearchEngine;
import ovh.equino.actracker.domain.tag.TagSearchResult;

class RepositoryTagSearchEngine implements TagSearchEngine {

    private final TagRepository tagRepository;

    RepositoryTagSearchEngine(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public TagSearchResult findTags(TagSearchCriteria searchCriteria) {
        return tagRepository.find(searchCriteria);
    }
}
