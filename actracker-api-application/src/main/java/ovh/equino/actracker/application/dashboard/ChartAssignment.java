package ovh.equino.actracker.application.dashboard;

import java.util.Set;
import java.util.UUID;

public record ChartAssignment(String name,
                              String groupBy,
                              String analysisMetric,
                              Set<UUID> includedTags) {
}
