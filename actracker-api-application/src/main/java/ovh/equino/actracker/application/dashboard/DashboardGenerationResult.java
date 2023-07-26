package ovh.equino.actracker.application.dashboard;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

public record DashboardGenerationResult(String name,
                                        Collection<GeneratedChart> charts) {

    public DashboardGenerationResult {
        charts = requireNonNullElse(charts, emptyList());
    }
}
