package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.CommonSearchCriteria;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public record TagSetSearchCriteria(

        // TODO remove unused fields
        CommonSearchCriteria common,
        String term,
        Instant timeRangeStart,
        Instant timeRangeEnd,
        Set<UUID> excludeFilter,
        Set<UUID> tags

) {

    private static final String DEFAULT_TERM = "";

    public TagSetSearchCriteria {
        requireNonNull(common);
        term = requireNonNullElse(term, DEFAULT_TERM);
        tags = requireNonNullElse(tags, emptySet());
    }
}
