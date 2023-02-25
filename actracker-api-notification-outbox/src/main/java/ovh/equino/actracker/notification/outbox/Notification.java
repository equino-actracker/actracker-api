package ovh.equino.actracker.notification.outbox;

import ovh.equino.actracker.domain.activity.ActivityDto;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

public record Notification(
        UUID id,
        ActivityDto entity) {

    public Notification {
        requireNonNull(id);
        requireNonNull(entity);
    }
}
