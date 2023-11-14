package ovh.equino.actracker.search.datasource.tag;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tag.TagDataSource;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagSearchEngine;

import java.util.LinkedList;
import java.util.List;

class DataSourceTagSearchEngine implements TagSearchEngine {

    private final TagDataSource tagDataSource;

    DataSourceTagSearchEngine(TagDataSource tagDataSource) {
        this.tagDataSource = tagDataSource;
    }

    @Override
    public EntitySearchResult<TagDto> findTags(EntitySearchCriteria searchCriteria) {

        EntitySearchCriteria forNextPageIdSearchCriteria = new EntitySearchCriteria(
                searchCriteria.searcher(),
                searchCriteria.pageSize() + 1,   // additional one to calculate next page ID
                searchCriteria.pageId(),
                searchCriteria.term(),
                searchCriteria.timeRangeStart(),
                searchCriteria.timeRangeEnd(),
                searchCriteria.excludeFilter(),
                searchCriteria.tags(),
                searchCriteria.sortCriteria()
        );

        List<TagDto> foundTags = tagDataSource.find(forNextPageIdSearchCriteria);
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
