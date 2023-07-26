package ovh.equino.actracker.application.activity;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNullElse;

public record ActivityResult(UUID id,
                             String title,
                             Instant startTime,
                             Instant endTime,
                             String comment,
                             Set<UUID> tags,
                             List<MetricValueResult> metricValues) {

    public ActivityResult {
        tags = requireNonNullElse(tags, emptySet());
        metricValues = requireNonNullElse(metricValues, emptyList());
    }
}
