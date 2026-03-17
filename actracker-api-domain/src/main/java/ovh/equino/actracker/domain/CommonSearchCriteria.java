package ovh.equino.actracker.domain;

import ovh.equino.actracker.domain.user.User;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public record CommonSearchCriteria(User searcher,
                                   Integer pageSize,
                                   String pageId) {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String DEFAULT_PAGE_ID = "";

    public CommonSearchCriteria {
        requireNonNull(searcher);
        pageSize = requireNonNullElse(pageSize, DEFAULT_PAGE_SIZE);
        pageId = requireNonNullElse(pageId, DEFAULT_PAGE_ID);
    }
}
