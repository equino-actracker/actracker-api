package ovh.equino.actracker.domain;

import ovh.equino.actracker.domain.user.User;

import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNullElse;

public record EntitySearchCriteria(

        User searcher,
        Integer pageSize,
        String pageId,
        String term,
        Set<UUID> excludeFilter

) {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String DEFAULT_PAGE_ID = "";
    private static final String DEFAULT_TERM = "";

    public EntitySearchCriteria {
        pageSize = requireNonNullElse(pageSize, DEFAULT_PAGE_SIZE);
        pageId = requireNonNullElse(pageId, DEFAULT_PAGE_ID);
        term = requireNonNullElse(term, DEFAULT_TERM);
    }
}
