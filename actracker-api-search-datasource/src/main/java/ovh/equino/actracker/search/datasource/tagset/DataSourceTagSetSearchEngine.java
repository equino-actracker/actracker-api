package ovh.equino.actracker.search.datasource.tagset;

import ovh.equino.actracker.domain.CommonSearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tagset.TagSetDataSource;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetSearchCriteria;
import ovh.equino.actracker.domain.tagset.TagSetSearchEngine;

import java.util.LinkedList;
import java.util.List;

class DataSourceTagSetSearchEngine implements TagSetSearchEngine {

    private final TagSetDataSource tagSetDataSource;

    DataSourceTagSetSearchEngine(TagSetDataSource tagSetDataSource) {
        this.tagSetDataSource = tagSetDataSource;
    }

    @Override
    public EntitySearchResult<TagSetDto> findTagSets(TagSetSearchCriteria searchCriteria) {
        var forNextPageIdSearchCriteria = new TagSetSearchCriteria(
                new CommonSearchCriteria(
                        searchCriteria.common().searcher(),
                        searchCriteria.common().pageSize() + 1,   // additional one to calculate next page ID
                        searchCriteria.common().pageId(),
                        searchCriteria.common().sortCriteria()
                ),
                searchCriteria.term(),
                searchCriteria.excludeFilter()
        );

        List<TagSetDto> foundTagSets = tagSetDataSource.find(forNextPageIdSearchCriteria);
        String nextPageId = getNextPageId(foundTagSets, searchCriteria.common().pageSize());
        List<TagSetDto> results = foundTagSets.stream()
                .limit(searchCriteria.common().pageSize())
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
