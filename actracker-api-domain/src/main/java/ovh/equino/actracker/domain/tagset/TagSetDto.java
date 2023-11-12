package ovh.equino.actracker.domain.tagset;

import java.util.Set;
import java.util.UUID;

public record TagSetDto(

        UUID id,
        UUID creatorId,
        String name,
        Set<UUID> tags,
        boolean deleted

) {

    // Constructor for data provided from input
    public TagSetDto(String name, Set<UUID> tags) {
        this(null, null, name, tags, false);
    }
}
