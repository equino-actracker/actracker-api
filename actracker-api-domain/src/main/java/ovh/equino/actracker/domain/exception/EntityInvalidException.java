package ovh.equino.actracker.domain.exception;

import ovh.equino.actracker.domain.Entity;

import java.util.List;

public final class EntityInvalidException extends RuntimeException {

    public EntityInvalidException(Class<? extends Entity> entityType, List<String> errors) {
        super(
                "%s invalid: %s"
                        .formatted(
                                entityType.getSimpleName(),
                                errors.toString()
                        )
        );
    }
}
