package ovh.equino.actracker.application.tagset;

import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNullElse;

public record TagSetResult(UUID id,
                           String name,
                           Set<UUID> tags) {

    public TagSetResult {
        tags = requireNonNullElse(tags, emptySet());
    }
}
