package ovh.equino.actracker.application.activity;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record SearchActivitiesQuery(Integer pageSize,
                                    String pageId,
                                    String term,
                                    Instant timeRangeStart,
                                    Instant timeRangeEnd,
                                    Set<UUID> tags,
                                    Set<UUID> excludeFilter) {
}
