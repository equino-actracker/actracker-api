package ovh.equino.actracker.application.activity;

import java.math.BigDecimal;
import java.util.UUID;

public record MetricValueResult(UUID metricId, BigDecimal value) {
}
