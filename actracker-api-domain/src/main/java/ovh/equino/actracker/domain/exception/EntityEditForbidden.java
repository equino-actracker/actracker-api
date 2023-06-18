package ovh.equino.actracker.domain.exception;

import ovh.equino.actracker.domain.Entity;

public class EntityEditForbidden extends RuntimeException {

    public EntityEditForbidden(Class<? extends Entity> entityType) {
        super(
                "Insufficient permission to edit %s"
                        .formatted(entityType.getSimpleName())
        );
    }
}
