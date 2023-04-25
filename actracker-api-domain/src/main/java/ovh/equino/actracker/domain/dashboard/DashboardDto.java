package ovh.equino.actracker.domain.dashboard;

import java.util.List;
import java.util.UUID;

public record DashboardDto(
        UUID id,
        UUID creatorId,
        String name,
        List<Chart> charts,
        boolean deleted
) {

    // Constructor for data provided from input
    public DashboardDto(String name, List<Chart> charts) {
        this(null, null, name, charts, false);
    }
}
