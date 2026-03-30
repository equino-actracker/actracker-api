package ovh.equino.actracker.domain;

import ovh.equino.actracker.domain.user.User;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static ovh.equino.actracker.domain.EntitySearchPageId.firstPage;

public record CommonSearchCriteria(User searcher,
                                   Integer pageSize,
                                   EntitySearchPageId pageId) {

    private static final int DEFAULT_PAGE_SIZE = 10;

    public CommonSearchCriteria {
        requireNonNull(searcher);
        pageSize = requireNonNullElse(pageSize, DEFAULT_PAGE_SIZE);
        pageId = requireNonNullElse(pageId, firstPage());
    }

}
