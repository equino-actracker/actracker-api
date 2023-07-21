package ovh.equino.actracker.application.tagset;

import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNullElse;

public record CreateTagSetCommand(String name,
                                  Set<UUID> tags
) {

    public CreateTagSetCommand {
        tags = requireNonNullElse(tags, emptySet());
    }
}
