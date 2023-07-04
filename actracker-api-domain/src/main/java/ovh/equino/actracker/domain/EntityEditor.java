package ovh.equino.actracker.domain;

import ovh.equino.actracker.domain.exception.EntityEditForbidden;
import ovh.equino.actracker.domain.user.User;

public abstract class EntityEditor<T extends Entity> {

    private final User editor;
    private final T entity;
    private final EntityEditOperation editOperation;

    protected EntityEditor(User editor, T entity, EntityEditOperation editOperation) {
        this.editor = editor;
        this.entity = entity;
        this.editOperation = editOperation;
    }

    public void run() {
        checkEditPermission();
        beforeEditOperation();
        editOperation.execute();
        entity.validate();
    }

    protected abstract void beforeEditOperation();

    private void checkEditPermission() {
        if(!entity.isEditableFor(editor)) {
            throw new EntityEditForbidden(entity.getClass());
        }
    }
}
