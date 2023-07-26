package ovh.equino.actracker.application.dashboard;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

public record DashboardResult(UUID id,
                              String name,
                              Collection<ChartResult> charts,
                              List<String> shares) {

    public DashboardResult {
        charts = requireNonNullElse(charts, emptyList());
        shares = requireNonNullElse(shares, emptyList());
    }
}
