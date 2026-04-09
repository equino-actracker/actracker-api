package ovh.equino.actracker.search.datasource;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySearchResult;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class DataSourceSearchEngine<T, S extends EntitySearchCriteria<T>> {

    private final NextPageIdExtractor.AttributeValueExtractor<T> attributeExtractor;

    protected DataSourceSearchEngine(NextPageIdExtractor.AttributeValueExtractor<T> attributeExtractor) {
        this.attributeExtractor = attributeExtractor;
    }

    protected final EntitySearchResult<T> findBy(S searchCriteria) {
        var pageSize = searchCriteria.common().pageSize();
        var pageId = searchCriteria.common().pageId();

        var nextPageIdTellingCommonCriteria = new EntitySearchCriteria.Common(
                searchCriteria.common().searcher(),
                searchCriteria.common().pageSize() + 1,   // additional one to calculate next page ID
                searchCriteria.common().pageId(),
                searchCriteria.common().sortCriteria());

        var nextPageIdTellingCriteria = withCommonCriteriaReplaced(searchCriteria, nextPageIdTellingCommonCriteria);
        var foundEntities = searchInDataSource(nextPageIdTellingCriteria);

        var results = foundEntities.stream()
                .limit(pageSize)
                .toList();

        var nextPageId = getNextPageId(foundEntities, pageSize, pageId);

        return new EntitySearchResult<>(nextPageId, results);
    }

    protected abstract List<T> searchInDataSource(S searchCriteria);

    protected abstract S withCommonCriteriaReplaced(S searchCriteria, EntitySearchCriteria.Common newCommonCriteria);

    private EntitySearchPageId getNextPageId(List<T> foundEntities, int pageSize, EntitySearchPageId previousPageId) {
        if (foundEntities.size() <= pageSize) {
            return null;
        }
        var nextPageEntity = foundEntities.get(pageSize);

        var nextPageIdValues = previousPageId.values().stream()
                .map(value -> toNextPageValue(value, nextPageEntity))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return new EntitySearchPageId(new LinkedList<>(nextPageIdValues));
    }

    private Optional<EntitySearchPageId.Value> toNextPageValue(EntitySearchPageId.Value value, T nextPageEntity) {
        return attributeExtractor.extractFieldAttribute(value.field(), nextPageEntity)
                .map(fieldValue -> new EntitySearchPageId.Value(value.field(), fieldValue));
    }
}
