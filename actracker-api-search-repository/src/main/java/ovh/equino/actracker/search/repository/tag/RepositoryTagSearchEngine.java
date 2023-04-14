package ovh.equino.actracker.search.repository.tag;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.domain.tag.TagSearchEngine;

import java.util.LinkedList;
import java.util.List;

class RepositoryTagSearchEngine implements TagSearchEngine {

    private final TagRepository tagRepository;

    RepositoryTagSearchEngine(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public EntitySearchResult<TagDto> findTags(EntitySearchCriteria searchCriteria) {

        EntitySearchCriteria forNextPageIdSearchCriteria = new EntitySearchCriteria(
                searchCriteria.searcher(),
                searchCriteria.pageSize() + 1,   // additional one to calculate next page ID
                searchCriteria.pageId(),
                searchCriteria.term(),
                searchCriteria.excludeFilter()
        );

        List<TagDto> foundTags = tagRepository.find(forNextPageIdSearchCriteria);
        String nextPageId = getNextPageId(foundTags, searchCriteria.pageSize());
        List<TagDto> results = foundTags.stream()
                .limit(searchCriteria.pageSize())
                .toList();

        return new EntitySearchResult<>(nextPageId, results);
    }

    private String getNextPageId(List<TagDto> foundTags, int pageSize) {
        if (foundTags.size() <= pageSize) {
            return null;
        }
        TagDto lastTag = new LinkedList<>(foundTags).get(pageSize);
        return lastTag.id().toString();
    }
}
