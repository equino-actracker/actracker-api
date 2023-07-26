package ovh.equino.actracker.application;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

public record SearchResult<T>(String nextPageId,
                              List<T> results) {

    public SearchResult {
        results = requireNonNullElse(results, emptyList());
    }
}
