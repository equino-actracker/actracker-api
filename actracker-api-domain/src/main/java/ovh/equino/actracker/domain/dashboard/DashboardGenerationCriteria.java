package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public record DashboardGenerationCriteria(

        UUID dashboardId,
        User generator,
        Instant timeRangeStart,
        Instant timeRangeEnd
) {

    public DashboardGenerationCriteria {
        requireNonNull(dashboardId);
        requireNonNull(generator);
    }
}
