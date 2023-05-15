package ovh.equino.actracker.rest.spring.tag;

record Metric(
        String name,
        MetricType type
) {

    enum MetricType {
        NUMERIC;

        static MetricType fromDomain(ovh.equino.actracker.domain.metric.MetricType metricType) {
            return switch (metricType) {
                case NUMERIC -> NUMERIC;
            };
        }

        static ovh.equino.actracker.domain.metric.MetricType toDomain(MetricType metricType) {
            return switch (metricType) {
                case NUMERIC -> ovh.equino.actracker.domain.metric.MetricType.NUMERIC;
            };
        }
    }
}
