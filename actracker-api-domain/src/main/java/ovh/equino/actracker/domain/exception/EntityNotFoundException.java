package ovh.equino.actracker.domain.exception;

import ovh.equino.actracker.domain.Entity;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(Class<? extends Entity> entityType, UUID entityId) {
        super(
                "Entity of type %s with ID %s not found"
                        .formatted(
                                entityType.getSimpleName(),
                                entityId.toString()
                        )
        );
    }
}
