package ovh.equino.actracker.domain.user;

import java.util.UUID;

import static java.util.Objects.isNull;

public record User(UUID id) {

    public User {
        if (isNull(id)) {
            throw new IllegalArgumentException("User.id must is null");
        }
    }

    public User(String id) {
        this(UUID.fromString(id));
    }
}
