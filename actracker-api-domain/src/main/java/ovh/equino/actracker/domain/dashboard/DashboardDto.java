package ovh.equino.actracker.domain.dashboard;

import java.util.UUID;

public record DashboardDto(
        UUID id,
        UUID creatorId,
        String name,
        boolean deleted
) {

    // Constructor for data provided from input
    public DashboardDto(String name) {
        this(null, null, name, false);
    }
}
