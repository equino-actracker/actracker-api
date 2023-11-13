package ovh.equino.actracker.repository.jpa.activity;

import java.math.BigDecimal;

record MetricValueProjection(String id, String activityId, String metricId, BigDecimal value) {
}
