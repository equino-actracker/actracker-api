package ovh.equino.actracker.rest.spring;

import ovh.equino.actracker.application.SearchResult;

import java.util.List;

import static ovh.equino.actracker.rest.spring.PayloadUtils.jsonValue;

public record SearchRestTestData<T>(String nextPageId,
                                    List<? extends RestTestData<T>> entities) {

    public String asHttpResponse() {
        return """
                {
                    "nextPageId": {nextPageId},
                    "results": {results}
                }
                """
                .replace("{nextPageId}", jsonValue(nextPageId))
                .replace("{results}", jsonValue(entities, RestTestData<T>::asHttpResponse));
    }

    public SearchResult<T> asEntitySearchResult() {
        var entityResults = entities.stream()
                .map(RestTestData<T>::asEntityResult)
                .toList();
        return new SearchResult<>(nextPageId, entityResults);
    }
}
