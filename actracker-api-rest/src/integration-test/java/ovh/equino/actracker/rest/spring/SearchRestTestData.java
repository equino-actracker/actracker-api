package ovh.equino.actracker.rest.spring;

import ovh.equino.actracker.application.SearchResult;

import java.util.List;

import static java.util.stream.Collectors.joining;
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
                .replace("{results}", httpResults());
    }

    public SearchResult<T> asEntitySearchResult() {
        var entityResults = entities.stream()
                .map(RestTestData<T>::asEntityResult)
                .toList();
        return new SearchResult<>(nextPageId, entityResults);
    }

    private String httpResults() {
        return entities.stream()
                .map(RestTestData<T>::asHttpResponse)
                .collect(joining(",", "[", "]"));
    }
}
