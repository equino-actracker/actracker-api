package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.EntityEditOperation;
import ovh.equino.actracker.domain.EntityModification;
import ovh.equino.actracker.domain.user.User;

class DashboardEditOperation extends EntityEditOperation<Dashboard> {

    protected DashboardEditOperation(User editor, Dashboard entity, EntityModification entityModification) {
        super(editor, entity, entityModification);
    }

    @Override
    protected void beforeEditOperation() {
    }

    @Override
    protected void afterEditOperation() {
    }
}
