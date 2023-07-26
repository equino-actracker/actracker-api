package ovh.equino.actracker.application.dashboard;

import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNullElse;

public record SearchDashboardsQuery(Integer pageSize,
                                    String pageId,
                                    String term,
                                    Set<UUID> excludeFilter) {

    public SearchDashboardsQuery {
        excludeFilter = requireNonNullElse(excludeFilter, emptySet());
    }
}
