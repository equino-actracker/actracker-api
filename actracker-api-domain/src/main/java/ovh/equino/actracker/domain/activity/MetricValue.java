package ovh.equino.actracker.domain.activity;

import java.math.BigDecimal;
import java.util.UUID;

public record MetricValue(
        UUID metricId,
        BigDecimal value
) {
}
