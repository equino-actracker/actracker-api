package ovh.equino.actracker.rest.spring.dashboard;

import java.util.Collection;

record Chart(

        String name,
        GroupBy groupBy,
        AnalysisMetric metric,
        Collection<String> includedTags

) {
}
