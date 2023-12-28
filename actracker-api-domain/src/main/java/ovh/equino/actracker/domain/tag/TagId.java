package ovh.equino.actracker.domain.tag;

import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;

public record TagId(UUID id) {

    public TagId {
        if (isNull(id)) {
            throw new IllegalArgumentException("TagId.id must not be null");
        }
    }

    public TagId(String id) {
        this(UUID.fromString(id));
    }

    public TagId() {
        this(randomUUID());
    }
}
