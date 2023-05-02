package ovh.equino.actracker.domain.dashboard;

import java.math.BigDecimal;
import java.util.Collection;

public record ChartBucketData(
        String name,
        BucketType type,
        BigDecimal value,
        BigDecimal percentage,
        Collection<ChartBucketData> buckets
) {

    public enum BucketType {
        TAG, DAY
    }
}
