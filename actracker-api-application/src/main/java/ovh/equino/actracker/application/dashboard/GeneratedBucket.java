package ovh.equino.actracker.application.dashboard;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

public record GeneratedBucket(String id,
                              Instant rangeStart,
                              Instant rangeEnd,
                              String bucketType,
                              BigDecimal value,
                              BigDecimal percentage,
                              Collection<GeneratedBucket> buckets) {

    public GeneratedBucket {
        buckets = requireNonNullElse(buckets, emptyList());
    }
}
