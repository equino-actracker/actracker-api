package ovh.equino.actracker.application.dashboard;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

public record GeneratedChart(String name,
                             Collection<GeneratedBucket> buckets) {

    public GeneratedChart {
        buckets = requireNonNullElse(buckets, emptyList());
    }
}
