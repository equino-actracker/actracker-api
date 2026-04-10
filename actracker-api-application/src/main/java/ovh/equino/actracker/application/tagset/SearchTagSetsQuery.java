package ovh.equino.actracker.application.tagset;

import ovh.equino.actracker.application.SortCriteria;

import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNullElse;

public record SearchTagSetsQuery(Integer pageSize,
                                 String pageId,
                                 String term,
                                 Set<UUID> excludeFilter,
                                 SortCriteria sortCriteria) {

    public SearchTagSetsQuery {
        excludeFilter = requireNonNullElse(excludeFilter, emptySet());
        sortCriteria = requireNonNullElse(sortCriteria, new SortCriteria());
    }
}
