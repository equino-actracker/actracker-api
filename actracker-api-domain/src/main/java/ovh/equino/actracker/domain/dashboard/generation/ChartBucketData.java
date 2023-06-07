package ovh.equino.actracker.domain.dashboard.generation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;

import static java.math.RoundingMode.HALF_UP;

public record ChartBucketData(
        String id,
        Instant rangeStart,
        Instant rangeEnd,
        BucketType bucketType,
        BigDecimal value,
        BigDecimal percentage,
        Collection<ChartBucketData> buckets
) {

    public ChartBucketData {
        if (percentage != null) {
            percentage = percentage.setScale(3, HALF_UP);
        }
    }

}
