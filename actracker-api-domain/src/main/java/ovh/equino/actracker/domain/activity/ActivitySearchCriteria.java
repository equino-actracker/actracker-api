package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.CommonSearchCriteria;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public record ActivitySearchCriteria(

        CommonSearchCriteria common,
        String term,
        Instant timeRangeStart,
        Instant timeRangeEnd,
        Set<UUID> excludeFilter,
        Set<UUID> tags

) {

    private static final String DEFAULT_TERM = "";

    public ActivitySearchCriteria {
        requireNonNull(common);
        term = requireNonNullElse(term, DEFAULT_TERM);
        tags = requireNonNullElse(tags, emptySet());
    }
}
