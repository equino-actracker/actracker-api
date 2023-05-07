package ovh.equino.actracker.domain;

import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static ovh.equino.actracker.domain.EntitySortCriteria.irrelevant;

public record EntitySearchCriteria(

        User searcher,
        Integer pageSize,
        String pageId,
        String term,
        Instant timeRangeStart,
        Instant timeRangeEnd,
        Set<UUID> excludeFilter,
        Set<UUID> tags,
        EntitySortCriteria sortCriteria

) {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String DEFAULT_PAGE_ID = "";
    private static final String DEFAULT_TERM = "";

    public EntitySearchCriteria {
        requireNonNull(searcher);
        pageSize = requireNonNullElse(pageSize, DEFAULT_PAGE_SIZE);
        pageId = requireNonNullElse(pageId, DEFAULT_PAGE_ID);
        term = requireNonNullElse(term, DEFAULT_TERM);
        sortCriteria = requireNonNullElse(sortCriteria, irrelevant());
        tags = requireNonNullElse(tags, emptySet());
    }
}
