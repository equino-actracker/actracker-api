package ovh.equino.actracker.domain.tagset;

import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;

public record TagSetId(UUID id) {

    public TagSetId {
        if (isNull(id)) {
            throw new IllegalArgumentException("TagSetId.id must not be null");
        }
    }

    public TagSetId(String id) {
        this(UUID.fromString(id));
    }

    TagSetId() {
        this(randomUUID());
    }
}
