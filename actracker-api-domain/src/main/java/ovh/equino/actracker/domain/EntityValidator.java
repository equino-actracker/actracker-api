package ovh.equino.actracker.domain;

import ovh.equino.actracker.domain.exception.EntityInvalidException;

import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

// TODO rename to EntityValidator after removing old entity validator
public abstract class EntityValidator<T extends Entity> {

    protected abstract Class<T> entityType();

    protected abstract List<String> collectValidationErrors(T entity);

    public final void validate(T entity) {
        List<String> validationErrors = collectValidationErrors(entity);
        if (isNotEmpty(validationErrors)) {
            throw new EntityInvalidException(entityType(), validationErrors);
        }
    }
}
