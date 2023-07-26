package ovh.equino.actracker.application.dashboard;

import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNullElse;

public record ChartAssignment(String name,
                              String groupBy,
                              String analysisMetric,
                              Set<UUID> includedTags) {

    public ChartAssignment {
        includedTags = requireNonNullElse(includedTags, emptySet());
    }
}
