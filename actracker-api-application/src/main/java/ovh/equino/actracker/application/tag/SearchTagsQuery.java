package ovh.equino.actracker.application.tag;

import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNullElse;

public record SearchTagsQuery(Integer pageSize,
                              String pageId,
                              String term,
                              Set<UUID> excludeFilter) {

    public SearchTagsQuery {
        excludeFilter = requireNonNullElse(excludeFilter, emptySet());
    }
}
