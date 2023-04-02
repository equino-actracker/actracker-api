package ovh.equino.actracker.domain;

import org.apache.commons.collections4.CollectionUtils;
import ovh.equino.actracker.domain.exception.EntityInvalidException;

import java.util.List;

public abstract class EntityValidator<T extends Entity> {

    public abstract Class<T> entityType();

    protected void handleValidationErrors(List<String> errors) {
        if (CollectionUtils.isNotEmpty(errors)) {
            throw new EntityInvalidException(entityType(), errors);
        }
    }
}
