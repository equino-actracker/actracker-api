package ovh.equino.actracker.application.dashboard;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNullElse;

public record GenerateDashboardQuery(UUID dashboardId,
                                     Instant timeRangeStart,
                                     Instant timeRangeEnd,
                                     Set<UUID> tags) {

    public GenerateDashboardQuery {
        tags = requireNonNullElse(tags, emptySet());
    }
}
