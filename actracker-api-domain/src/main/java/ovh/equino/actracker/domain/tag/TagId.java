package ovh.equino.actracker.domain.tag;

import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;

record TagId(UUID id) {

    TagId {
        if (isNull(id)) {
            throw new IllegalArgumentException("TagId.id must not be null");
        }
    }

    TagId() {
        this(randomUUID());
    }
}
