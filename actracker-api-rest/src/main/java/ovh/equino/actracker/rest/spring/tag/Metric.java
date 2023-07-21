package ovh.equino.actracker.rest.spring.tag;

record Metric(
        String id,
        String name,
        String type
) {

    enum MetricType {
        NUMERIC;

        static MetricType fromDomain(ovh.equino.actracker.domain.tag.MetricType metricType) {
            return switch (metricType) {
                case NUMERIC -> NUMERIC;
            };
        }

        static ovh.equino.actracker.domain.tag.MetricType toDomain(MetricType metricType) {
            return switch (metricType) {
                case NUMERIC -> ovh.equino.actracker.domain.tag.MetricType.NUMERIC;
            };
        }
    }
}
