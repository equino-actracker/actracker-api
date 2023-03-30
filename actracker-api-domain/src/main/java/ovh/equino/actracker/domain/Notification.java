package ovh.equino.actracker.domain;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

public record Notification<T> (
        UUID id,
        long version,
        T data,
        Class<?> notificationType) {

    public Notification(UUID id, long version, T data) {
        this(id, version, data, data.getClass());
    }

    public Notification(UUID id, T data) {
        // Version must be not null, otherwise H2 fails
        this(id, 0L, data);
    }

    public Notification {
        requireNonNull(id);
        requireNonNull(data);
    }

}
