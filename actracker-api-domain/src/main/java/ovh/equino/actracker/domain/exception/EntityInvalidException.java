package ovh.equino.actracker.domain.exception;

import ovh.equino.actracker.domain.Entity;

import java.util.List;

import static java.util.Collections.singletonList;

public final class EntityInvalidException extends RuntimeException {

    public EntityInvalidException(Class<? extends Entity> entityType, String error) {
        this(entityType, singletonList(error));
    }

    public EntityInvalidException(Class<? extends Entity> entityType, List<String> errors) {
        super(
                "%s invalid: %s"
                        .formatted(
                                entityType.getSimpleName(),
                                errors
                        )
        );
    }
}
