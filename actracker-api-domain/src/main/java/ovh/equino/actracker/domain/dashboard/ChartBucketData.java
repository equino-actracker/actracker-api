package ovh.equino.actracker.domain.dashboard;

import java.math.BigDecimal;
import java.util.Collection;

public record ChartBucketData(
        String name,
        BigDecimal value,
        BigDecimal percentage,
        Collection<ChartBucketData> buckets
) {
}
