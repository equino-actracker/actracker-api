package ovh.equino.actracker.application.tag;

import java.util.Set;
import java.util.UUID;

public record SearchTagsQuery(Integer pageSize,
                              String pageId,
                              String term,
                              Set<UUID> excludeFilter) {
}
