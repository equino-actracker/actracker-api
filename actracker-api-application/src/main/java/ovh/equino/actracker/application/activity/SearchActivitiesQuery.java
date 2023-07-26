package ovh.equino.actracker.application.activity;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNullElse;

public record SearchActivitiesQuery(Integer pageSize,
                                    String pageId,
                                    String term,
                                    Instant timeRangeStart,
                                    Instant timeRangeEnd,
                                    Set<UUID> tags,
                                    Set<UUID> excludeFilter) {

    public SearchActivitiesQuery {
        tags = requireNonNullElse(tags, emptySet());
        excludeFilter = requireNonNullElse(excludeFilter, emptySet());
    }
}
