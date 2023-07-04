package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.EntityModification;
import ovh.equino.actracker.domain.EntityEditOperation;
import ovh.equino.actracker.domain.user.User;

class TagSetEditOperation extends EntityEditOperation<TagSet> {

    protected TagSetEditOperation(User editor, TagSet entity, EntityModification entityModification) {
        super(editor, entity, entityModification);
    }

    @Override
    protected void beforeEditOperation() {
    }

    @Override
    protected void afterEditOperation() {

    }
}
