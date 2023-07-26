package ovh.equino.actracker.application.dashboard;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

public record CreateDashboardCommand(String name,
                                     Collection<ChartAssignment> chartAssignments,
                                     Collection<String> shares) {

    public CreateDashboardCommand {
        chartAssignments = requireNonNullElse(chartAssignments, emptyList());
        shares = requireNonNullElse(shares, emptyList());
    }
}
