package ovh.equino.actracker.domain.tagset;

import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNullElse;

public record TagSetDto(

        UUID id,
        UUID creatorId,
        String name,
        Set<UUID> tags,
        boolean deleted

) {

    public TagSetDto {
        tags = requireNonNullElse(tags, emptySet());
    }

    // Constructor for data provided from input
    public TagSetDto(String name, Set<UUID> tags) {
        this(null, null, name, tags, false);
    }
}
