package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.EntityEditOperation;
import ovh.equino.actracker.domain.EntityModification;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.user.User;

import java.util.Set;

class TagSetEditOperation extends EntityEditOperation<TagSet> {

    private final TagsExistenceVerifier tagsExistenceVerifier;
    private Set<TagId> tagsToPreserve;

    protected TagSetEditOperation(User editor,
                                  TagSet entity,
                                  TagsExistenceVerifier tagsExistenceVerifier,
                                  EntityModification entityModification) {

        super(editor, entity, entityModification);
        this.tagsExistenceVerifier = tagsExistenceVerifier;
    }

    @Override
    protected void beforeEditOperation() {
        tagsToPreserve = tagsExistenceVerifier.notExisting(entity.tags);
        Set<TagId> existingTags = tagsExistenceVerifier.existing(entity.tags);
        entity.tags.clear();
        entity.tags.addAll(existingTags);
    }

    @Override
    protected void afterEditOperation() {
        entity.tags.addAll(tagsToPreserve);
    }
}
