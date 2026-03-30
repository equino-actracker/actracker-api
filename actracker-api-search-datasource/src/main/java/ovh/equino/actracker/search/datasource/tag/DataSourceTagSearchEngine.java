package ovh.equino.actracker.search.datasource.tag;

import ovh.equino.actracker.domain.CommonSearchCriteria;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tag.TagDataSource;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;
import ovh.equino.actracker.domain.tag.TagSearchEngine;

import java.util.LinkedList;
import java.util.List;

import static ovh.equino.actracker.domain.EntitySearchPageId.aPageId;

class DataSourceTagSearchEngine implements TagSearchEngine {

    private final TagDataSource tagDataSource;

    DataSourceTagSearchEngine(TagDataSource tagDataSource) {
        this.tagDataSource = tagDataSource;
    }

    @Override
    public EntitySearchResult<TagDto> findTags(TagSearchCriteria searchCriteria) {

        var requestedPageId = searchCriteria.common().pageId();

        var forNextPageIdSearchCriteria = new TagSearchCriteria(
                new CommonSearchCriteria(
                        searchCriteria.common().searcher(),
                        searchCriteria.common().pageSize() + 1,   // additional one to calculate next page ID
                        requestedPageId
                ),
                searchCriteria.term(),
                searchCriteria.excludeFilter()
        );

        var foundTags = tagDataSource.find(forNextPageIdSearchCriteria);
        var nextPageId = getNextPageId(foundTags, searchCriteria.common().pageSize(), requestedPageId);
        var results = foundTags.stream()
                .limit(searchCriteria.common().pageSize())
                .toList();

        return new EntitySearchResult<>(nextPageId, results);
    }

    private String getNextPageId(List<TagDto> foundTags, int pageSize, EntitySearchPageId previousPageId) {
        if (foundTags.size() <= pageSize) {
            return null;
        }

        var pageId = aPageId();
        // TODO calculate basing on lastTag and previousPageId

        var lastTag = new LinkedList<>(foundTags).get(pageSize);
        return lastTag.id().toString();
    }
}
