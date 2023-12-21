package ovh.equino.actracker.search.datasource.tagset;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tagset.TagSetDataSource;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetSearchEngine;

import java.util.LinkedList;
import java.util.List;

class DataSourceTagSetSearchEngine implements TagSetSearchEngine {

    private final TagSetDataSource tagSetDataSource;

    DataSourceTagSetSearchEngine(TagSetDataSource tagSetDataSource) {
        this.tagSetDataSource = tagSetDataSource;
    }

    @Override
    public EntitySearchResult<TagSetDto> findTagSets(EntitySearchCriteria searchCriteria) {
        EntitySearchCriteria forNextPageIdSearchCriteria = new EntitySearchCriteria(
                searchCriteria.searcher(),
                searchCriteria.pageSize() + 1,   // additional one to calculate next page ID
                searchCriteria.pageId(),
                searchCriteria.term(),
                searchCriteria.timeRangeStart(),
                searchCriteria.timeRangeEnd(),
                searchCriteria.excludeFilter(),
                searchCriteria.tags()
        );

        List<TagSetDto> foundTagSets = tagSetDataSource.find(forNextPageIdSearchCriteria);
        String nextPageId = getNextPageId(foundTagSets, searchCriteria.pageSize());
        List<TagSetDto> results = foundTagSets.stream()
                .limit(searchCriteria.pageSize())
                .toList();

        return new EntitySearchResult<>(nextPageId, results);
    }

    private String getNextPageId(List<TagSetDto> foundTagSets, int pageSize) {
        if (foundTagSets.size() <= pageSize) {
            return null;
        }
        TagSetDto lastTagSet = new LinkedList<>(foundTagSets).get(pageSize);
        return lastTagSet.id().toString();
    }
}
