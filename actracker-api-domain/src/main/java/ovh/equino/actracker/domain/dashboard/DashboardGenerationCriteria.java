package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.UUID;

public record DashboardGenerationCriteria(

        UUID dashboardId,
        User generator,
        Instant timeRangeStart,
        Instant timeRangeEnd
) {
}
