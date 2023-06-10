package ovh.equino.actracker.rest.spring.dashboard;

enum AnalysisMetric {
    TAG_PERCENTAGE,
    TAG_DURATION,
    METRIC_VALUE;

    static AnalysisMetric fromDomain(ovh.equino.actracker.domain.dashboard.AnalysisMetric metric) {
        return switch (metric) {
            case TAG_PERCENTAGE -> TAG_PERCENTAGE;
            case TAG_DURATION -> TAG_DURATION;
            case METRIC_VALUE -> METRIC_VALUE;
        };
    }

    static ovh.equino.actracker.domain.dashboard.AnalysisMetric toDomain(AnalysisMetric metric) {
        return switch (metric) {
            case TAG_PERCENTAGE -> ovh.equino.actracker.domain.dashboard.AnalysisMetric.TAG_PERCENTAGE;
            case TAG_DURATION -> ovh.equino.actracker.domain.dashboard.AnalysisMetric.TAG_DURATION;
            case METRIC_VALUE -> ovh.equino.actracker.domain.dashboard.AnalysisMetric.METRIC_VALUE;
        };
    }
}
