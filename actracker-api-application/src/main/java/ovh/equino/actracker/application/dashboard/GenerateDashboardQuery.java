package ovh.equino.actracker.application.dashboard;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record GenerateDashboardQuery(UUID dashboardId,
                                     Instant timeRangeStart,
                                     Instant timeRangeEnd,
                                     Set<UUID> tags) {
}
