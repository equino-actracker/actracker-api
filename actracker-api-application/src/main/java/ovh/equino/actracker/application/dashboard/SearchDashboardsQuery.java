package ovh.equino.actracker.application.dashboard;

import java.util.Set;
import java.util.UUID;

public record SearchDashboardsQuery(Integer pageSize,
                                    String pageId,
                                    String term,
                                    Set<UUID> excludeFilter) {
}
