package ovh.equino.actracker.domain;

import ovh.equino.actracker.domain.exception.EntityEditForbidden;
import ovh.equino.actracker.domain.user.User;

public abstract class EntityEditOperation<T extends Entity> {

    private final User editor;
    protected final T entity;
    private final EntityModification entityModification;

    protected EntityEditOperation(User editor, T entity, EntityModification entityModification) {
        this.editor = editor;
        this.entity = entity;
        this.entityModification = entityModification;
    }

    public void execute() {
        checkEditPermission();
        beforeEditOperation();
        entityModification.execute();
        entity.validate();
        afterEditOperation();
    }

    protected abstract void beforeEditOperation();

    protected abstract void afterEditOperation();

    private void checkEditPermission() {
        if(!entity.isEditableFor(editor)) {
            throw new EntityEditForbidden(entity.getClass());
        }
    }
}
