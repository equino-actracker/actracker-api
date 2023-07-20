package ovh.equino.actracker.application.tagset;

import java.util.Set;
import java.util.UUID;

public record CreateTagSetCommand(String name,
                                  Set<UUID> tags
) {
}
