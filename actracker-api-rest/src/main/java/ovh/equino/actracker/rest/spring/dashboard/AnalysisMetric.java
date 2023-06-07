package ovh.equino.actracker.rest.spring.dashboard;

enum AnalysisMetric {
    TAG_PERCENTAGE;

    static AnalysisMetric fromDomain(ovh.equino.actracker.domain.dashboard.AnalysisMetric metric) {
        return switch (metric) {
            case TAG_PERCENTAGE -> TAG_PERCENTAGE;
        };
    }

    static ovh.equino.actracker.domain.dashboard.AnalysisMetric toDomain(AnalysisMetric metric) {
        return switch (metric) {
            case TAG_PERCENTAGE -> ovh.equino.actracker.domain.dashboard.AnalysisMetric.TAG_PERCENTAGE;
        };
    }
}
