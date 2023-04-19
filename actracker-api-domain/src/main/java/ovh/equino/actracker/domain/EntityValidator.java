package ovh.equino.actracker.domain;

import ovh.equino.actracker.domain.exception.EntityInvalidException;

import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public abstract class EntityValidator<T extends Entity> {

    protected abstract Class<T> entityType();

    protected abstract List<String> collectValidationErrors();

    public final void validate() {
        List<String> validationErrors = collectValidationErrors();
        if (isNotEmpty(validationErrors)) {
            throw new EntityInvalidException(entityType(), validationErrors);
        }
    }
}
