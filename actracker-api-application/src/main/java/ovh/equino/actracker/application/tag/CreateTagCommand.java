package ovh.equino.actracker.application.tag;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

public record CreateTagCommand(String tagName,
                               Collection<MetricAssignment> metricAssignments,
                               Collection<String> grantedShares) {

    public CreateTagCommand {
        metricAssignments = requireNonNullElse(metricAssignments, emptyList());
        grantedShares = requireNonNullElse(grantedShares, emptyList());
    }
}
