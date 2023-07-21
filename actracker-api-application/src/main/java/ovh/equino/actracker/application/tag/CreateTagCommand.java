package ovh.equino.actracker.application.tag;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

public record CreateTagCommand(String tagName,
                               Collection<AssignedMetric> assignedMetrics,
                               Collection<String> grantedShares) {

    public CreateTagCommand {
        assignedMetrics = requireNonNullElse(assignedMetrics, emptyList());
        grantedShares = requireNonNullElse(grantedShares, emptyList());
    }
}
