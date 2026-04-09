package ovh.equino.actracker.domain;

import java.util.List;

public record EntitySearchResult<T>(

        EntitySearchPageId nextPageId,
        List<T> results
) {
}
