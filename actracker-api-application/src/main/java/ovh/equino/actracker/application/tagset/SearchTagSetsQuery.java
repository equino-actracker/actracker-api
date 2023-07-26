package ovh.equino.actracker.application.tagset;

import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNullElse;

public record SearchTagSetsQuery(Integer pageSize,
                                 String pageId,
                                 String term,
                                 Set<UUID> excludeFilter) {

    public SearchTagSetsQuery {
        excludeFilter = requireNonNullElse(excludeFilter, emptySet());
    }
}
