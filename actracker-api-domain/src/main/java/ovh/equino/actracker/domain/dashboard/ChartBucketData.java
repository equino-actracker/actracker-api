package ovh.equino.actracker.domain.dashboard;

import java.math.BigDecimal;
import java.util.Collection;

import static java.math.RoundingMode.HALF_UP;

public record ChartBucketData(
        String name,
        BucketType type,
        BigDecimal value,
        BigDecimal percentage,
        Collection<ChartBucketData> buckets
) {

    public ChartBucketData {
        if (percentage != null) {
            percentage = percentage.setScale(3, HALF_UP);
        }
    }

    public enum BucketType {
        TAG, DAY
    }
}
