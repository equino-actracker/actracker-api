package ovh.equino.actracker.application.activity;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ActivityResult(UUID id,
                             String title,
                             Instant startTime,
                             Instant endTime,
                             String comment,
                             Set<UUID> tags,
                             List<MetricValueResult> metricValues
) {
}
