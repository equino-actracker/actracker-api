package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.CommonSearchCriteria;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public record TagSearchCriteria(

        // TODO remove unused fields
        CommonSearchCriteria common,
        String term,
        Instant timeRangeStart,
        Instant timeRangeEnd,
        Set<UUID> excludeFilter,
        Set<UUID> tags

) {

    private static final String DEFAULT_TERM = "";

    public TagSearchCriteria {
        requireNonNull(common);
        term = requireNonNullElse(term, DEFAULT_TERM);
        tags = requireNonNullElse(tags, emptySet());
    }
}
