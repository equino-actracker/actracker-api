package ovh.equino.actracker.rest.spring.activity;

import java.math.BigDecimal;

record MetricValue(
        String metricId,
        BigDecimal value
) {
}
