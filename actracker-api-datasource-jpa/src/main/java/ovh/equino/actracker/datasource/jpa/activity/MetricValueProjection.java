package ovh.equino.actracker.datasource.jpa.activity;

import ovh.equino.actracker.domain.activity.MetricValue;

import java.math.BigDecimal;
import java.util.UUID;

record MetricValueProjection(String id, String activityId, String metricId, BigDecimal value) {

    MetricValue toMetricValue() {
        return new MetricValue(
                UUID.fromString(metricId()),
                value()
        );
    }
}
