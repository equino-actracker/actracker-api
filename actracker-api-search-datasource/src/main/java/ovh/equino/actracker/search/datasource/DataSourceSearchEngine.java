package ovh.equino.actracker.search.datasource;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.EntitySortCriteria;

import java.util.List;

public abstract class DataSourceSearchEngine<T, S extends EntitySearchCriteria<T>> {

    private final NextPageIdExtractor<T> nextPageIdExtractor;

    protected DataSourceSearchEngine(NextPageIdExtractor.AttributeValueExtractor<T> attributeExtractor) {
        this.nextPageIdExtractor = new NextPageIdExtractor<>(attributeExtractor);
    }

    protected final EntitySearchResult<T> findBy(S searchCriteria) {
        var pageSize = searchCriteria.common().pageSize();
        var sortCriteria = searchCriteria.common().sortCriteria();

        var nextPageIdTellingCommonCriteria = new EntitySearchCriteria.Common(
                searchCriteria.common().searcher(),
                searchCriteria.common().pageSize() + 1,   // additional one to calculate next page ID
                searchCriteria.common().pageId(),
                sortCriteria);

        var nextPageIdTellingCriteria = withCommonCriteriaReplaced(searchCriteria, nextPageIdTellingCommonCriteria);
        var foundEntities = searchInDataSource(nextPageIdTellingCriteria);

        var results = foundEntities.stream()
                .limit(pageSize)
                .toList();

        var nextPageId = getNextPageId(foundEntities, pageSize, sortCriteria);

        return new EntitySearchResult<>(nextPageId, results);
    }

    protected abstract List<T> searchInDataSource(S searchCriteria);

    protected abstract S withCommonCriteriaReplaced(S searchCriteria, EntitySearchCriteria.Common newCommonCriteria);

    private EntitySearchPageId getNextPageId(List<T> foundEntities, int pageSize, EntitySortCriteria sortCriteria) {
        if (foundEntities.size() <= pageSize) {
            return null;
        }
        var nextPageEntity = foundEntities.get(pageSize);
        return nextPageIdExtractor.nextPageId(sortCriteria, nextPageEntity);
    }
}
