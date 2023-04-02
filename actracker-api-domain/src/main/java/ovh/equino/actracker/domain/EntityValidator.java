package ovh.equino.actracker.domain;

import org.apache.commons.collections4.CollectionUtils;
import ovh.equino.actracker.domain.exception.EntityInvalidException;

import java.util.List;

public abstract class EntityValidator<T extends Entity> {

    protected abstract Class<T> entityType();

    protected abstract List<String> collectValidationErrors();

    public void validate() {
        List<String> validationErrors = collectValidationErrors();
        if (CollectionUtils.isNotEmpty(validationErrors)) {
            throw new EntityInvalidException(entityType(), validationErrors);
        }
    }
}
