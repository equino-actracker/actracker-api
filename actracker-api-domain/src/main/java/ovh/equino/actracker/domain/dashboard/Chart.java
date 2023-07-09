package ovh.equino.actracker.domain.dashboard;

import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNullElse;

public record Chart(

        ChartId id,
        String name,
        GroupBy groupBy,
        AnalysisMetric analysisMetric,
        Set<UUID> includedTags,
        boolean isDeleted
) {

    private static final boolean DELETED = true;

    public Chart {
        includedTags = requireNonNullElse(includedTags, emptySet());
        if (analysisMetric == null) {
            throw new IllegalArgumentException("Analysis metric cannot be null");
        }
    }

    public Chart(String name, GroupBy groupBy, AnalysisMetric analysisMetric, Set<UUID> includedTags) {
        this(new ChartId(), name, groupBy, analysisMetric, includedTags, !DELETED);
    }

    Chart deleted() {
        return new Chart(id, name, groupBy, analysisMetric, includedTags, DELETED);
    }

    public boolean includesAllTags() {
        return includedTags.isEmpty();
    }

}
