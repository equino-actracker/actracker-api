package ovh.equino.actracker.rest.spring;

import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.isNull;

abstract public class PayloadMapper {

    protected Instant timestampToInstant(Long timestamp) {
        if (isNull(timestamp)) {
            return null;
        }
        return Instant.ofEpochMilli(timestamp);
    }

    protected Long instantToTimestamp(Instant instant) {
        if (isNull(instant)) {
            return null;
        }
        return instant.toEpochMilli();
    }

    protected String uuidToString(UUID uuid) {
        if (isNull(uuid)) {
            return null;
        }
        return uuid.toString();
    }
}
