package ovh.equino.actracker.domain.dashboard;

import java.util.Set;
import java.util.UUID;

import static java.util.UUID.randomUUID;

public record ChartTestData(UUID id,
                            String name,
                            GroupBy groupBy,
                            AnalysisMetric analysisMetric,
                            Set<UUID> includedTags,
                            boolean deleted) {

    public static ChartTestData aChart() {
        return new ChartTestData(
                randomUUID(),
                "nameless chart",
                GroupBy.SELF,
                AnalysisMetric.METRIC_VALUE,
                Set.of(randomUUID(), randomUUID()),
                false
        );
    }

    public Chart asDto() {
        return new Chart(new ChartId(id), name, groupBy, analysisMetric, includedTags, deleted);
    }

}
