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
import java.util.Optional;

class DataSourceTagSearchEngine implements TagSearchEngine {

    private final TagDataSource tagDataSource;
    private final TagAttributeExtractor tagAttributeExtractor;

    DataSourceTagSearchEngine(TagDataSource tagDataSource) {
        this.tagDataSource = tagDataSource;
        this.tagAttributeExtractor = new TagAttributeExtractor();
    }

    @Override
    public EntitySearchResult<TagDto> findTags(TagSearchCriteria searchCriteria) {

        var requestedPageId = searchCriteria.common().pageId();

        var forNextPageIdSearchCriteria = new TagSearchCriteria(
                new CommonSearchCriteria(
                        searchCriteria.common().searcher(),
                        searchCriteria.common().pageSize() + 1,   // additional one to calculate next page ID
                        requestedPageId,
                        searchCriteria.common().sortCriteria()
                ),
                searchCriteria.term(),
                searchCriteria.excludeFilter()
        );

        var foundTags = tagDataSource.find(forNextPageIdSearchCriteria);
        var results = foundTags.stream()
                .limit(searchCriteria.common().pageSize())
                .toList();
        var nextPageId = getNextPageId(foundTags, searchCriteria.common().pageSize(), requestedPageId);

        return new EntitySearchResult<>(nextPageId, results);
    }

    private String getNextPageId(List<TagDto> foundTags, int pageSize, EntitySearchPageId previousPageId) {
        if (foundTags.size() <= pageSize) {
            return null;
        }
        var nextPageTag = foundTags.get(pageSize);

        var nextPageIdValues = previousPageId.values().stream()
                .map(value -> toNextPageValue(value, nextPageTag))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        var nextPageId = new EntitySearchPageId(new LinkedList<>(nextPageIdValues));
        // TODO calculate basing on lastTag and previousPageId

        var lastTag = new LinkedList<>(foundTags).get(pageSize);
        return lastTag.id().toString();
    }

    private Optional<EntitySearchPageId.Value> toNextPageValue(EntitySearchPageId.Value value, TagDto nextPageTag) {
        var maybeFieldValue = tagAttributeExtractor.extractFieldAttribute(value.field(), nextPageTag);
        return maybeFieldValue.map(fieldValue -> new EntitySearchPageId.Value(value.field(), fieldValue));
    }
}
