package ovh.equino.actracker.notification.outbox;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

public record Notification(
        UUID id,
        Object entity) {

    public Notification {
        requireNonNull(id);
        requireNonNull(entity);
    }
}
