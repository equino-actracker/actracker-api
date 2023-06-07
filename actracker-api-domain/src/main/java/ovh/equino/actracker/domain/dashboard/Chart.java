package ovh.equino.actracker.domain.dashboard;

import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNullElse;

public record Chart(

        String name,
        GroupBy groupBy,
        AnalysisMetric analysisMetric,
        Set<UUID> includedTags
) {

    public Chart {
        includedTags = requireNonNullElse(includedTags, emptySet());
        if (analysisMetric == null) {
            throw new IllegalArgumentException("Analysis metric cannot be null");
        }
    }

    public boolean includesAllTags() {
        return includedTags.isEmpty();
    }

}
