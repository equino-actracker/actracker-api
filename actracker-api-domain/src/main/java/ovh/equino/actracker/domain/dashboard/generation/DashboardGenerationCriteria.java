package ovh.equino.actracker.domain.dashboard.generation;

import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public record DashboardGenerationCriteria(

        UUID dashboardId,
        User generator,
        Instant timeRangeStart,
        Instant timeRangeEnd,
        Set<UUID> tags
) {

    public DashboardGenerationCriteria {
        requireNonNull(dashboardId);
        requireNonNull(generator);
        tags = requireNonNullElse(tags, emptySet());
    }
}
