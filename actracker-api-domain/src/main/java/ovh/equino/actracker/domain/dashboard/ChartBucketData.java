package ovh.equino.actracker.domain.dashboard;

import java.math.BigDecimal;

public record ChartBucketData(
        String name,
        BigDecimal value,
        BigDecimal percentage
) {
}
