package ovh.equino.actracker.domain.tag;

import java.util.UUID;

public record TagDto(

        UUID id,
        UUID creatorId,
        String name,
        boolean deleted

) {

    // Constructor for data provided from input
    public TagDto(String name) {
        this(null, null, name, false);
    }

}
