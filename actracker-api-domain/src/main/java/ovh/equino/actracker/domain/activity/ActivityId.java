package ovh.equino.actracker.domain.activity;

import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;

public record ActivityId(UUID id) {

    public ActivityId {
        if (isNull(id)) {
            throw new IllegalArgumentException("ActivityId.id must not be null");
        }
    }

    public ActivityId(String id) {
        this(UUID.fromString(id));
    }

    ActivityId() {
        this(randomUUID());
    }
}
