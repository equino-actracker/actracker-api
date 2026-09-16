package ovh.equino.actracker.application.activity;

import java.math.BigDecimal;
import java.util.UUID;

// TODO This is a value object, does it really have to be mapped? Removing it requires REST module depending on domain.
public record MetricValueResult(UUID metricId, BigDecimal value) {
}
