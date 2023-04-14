package ovh.equino.actracker.domain;

import java.util.List;

public record EntitySearchResult<T>(

        String nextPageId,
        List<T> results
) {
}
