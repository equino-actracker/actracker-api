package ovh.equino.actracker.domain.activity;

import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;

record ActivityId(UUID id) {

    ActivityId {
        if (isNull(id)) {
            throw new IllegalArgumentException("ActivityId.id must not be null");
        }
    }

    ActivityId() {
        this(randomUUID());
    }
}
