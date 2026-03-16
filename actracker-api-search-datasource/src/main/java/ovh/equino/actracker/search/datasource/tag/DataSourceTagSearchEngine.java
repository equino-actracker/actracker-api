package ovh.equino.actracker.search.datasource.tag;

import ovh.equino.actracker.domain.CommonSearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tag.TagDataSource;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;
import ovh.equino.actracker.domain.tag.TagSearchEngine;

import java.util.LinkedList;
import java.util.List;

class DataSourceTagSearchEngine implements TagSearchEngine {

    private final TagDataSource tagDataSource;

    DataSourceTagSearchEngine(TagDataSource tagDataSource) {
        this.tagDataSource = tagDataSource;
    }

    @Override
    public EntitySearchResult<TagDto> findTags(TagSearchCriteria searchCriteria) {

        var forNextPageIdSearchCriteria = new TagSearchCriteria(
                new CommonSearchCriteria(
                        searchCriteria.common().searcher(),
                        searchCriteria.common().pageSize() + 1,   // additional one to calculate next page ID
                        searchCriteria.common().pageId()
                ),
                searchCriteria.term(),
                searchCriteria.timeRangeStart(),
                searchCriteria.timeRangeEnd(),
                searchCriteria.excludeFilter(),
                searchCriteria.tags()
        );

        List<TagDto> foundTags = tagDataSource.find(forNextPageIdSearchCriteria);
        String nextPageId = getNextPageId(foundTags, searchCriteria.common().pageSize());
        List<TagDto> results = foundTags.stream()
                .limit(searchCriteria.common().pageSize())
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
