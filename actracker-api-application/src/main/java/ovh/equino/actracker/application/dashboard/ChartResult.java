package ovh.equino.actracker.application.dashboard;

import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNullElse;

public record ChartResult(UUID id,
                          String name,
                          String groupBy,
                          String analysisMetric,
                          Set<UUID> includedTags) {

    public ChartResult {
        includedTags = requireNonNullElse(includedTags, emptySet());
    }
}
