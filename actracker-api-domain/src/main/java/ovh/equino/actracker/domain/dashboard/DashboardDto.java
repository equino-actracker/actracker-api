package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.share.Share;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;

public record DashboardDto(
        UUID id,
        UUID creatorId,
        String name,
        List<Chart> charts,
        List<Share> shares,
        boolean deleted
) {

    // Constructor for data provided from input
    public DashboardDto(String name, List<Chart> charts) {
        this(null, null, name, charts, emptyList(), false);
    }
}
