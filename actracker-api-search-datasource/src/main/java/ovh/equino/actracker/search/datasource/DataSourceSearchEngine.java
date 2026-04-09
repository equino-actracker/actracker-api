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

        var foundEntities = findPage(forNextPageIdSearchCriteria(searchCriteria));
        var results = foundEntities.stream()
                .limit(pageSize)
                .toList();

        var nextPageId = getNextPageId(foundEntities, pageSize, pageId);

        return new EntitySearchResult<>(nextPageId, results);
    }

    protected abstract List<T> findPage(S searchCriteria);

    // TODO remove, calculate nextPageId using last entity of current page
    protected abstract S forNextPageIdSearchCriteria(S searchCriteria);

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
