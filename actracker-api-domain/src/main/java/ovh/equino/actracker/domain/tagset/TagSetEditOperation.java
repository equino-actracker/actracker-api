package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.EntityEditOperation;
import ovh.equino.actracker.domain.EntityModification;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.User;

import java.util.Set;

class TagSetEditOperation extends EntityEditOperation<TagSet> {

    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;
    private Set<TagId> tagsToPreserve;

    protected TagSetEditOperation(User editor,
                                  TagSet entity,
                                  TagsAccessibilityVerifier tagsAccessibilityVerifier,
                                  EntityModification entityModification) {

        super(editor, entity, entityModification);
        this.tagsAccessibilityVerifier = tagsAccessibilityVerifier;
    }

    @Override
    protected void beforeEditOperation() {
        tagsToPreserve = tagsAccessibilityVerifier.nonAccessibleOf(entity.tags);
        Set<TagId> existingTags = tagsAccessibilityVerifier.accessibleOf(entity.tags);
        entity.tags.clear();
        entity.tags.addAll(existingTags);
    }

    @Override
    protected void afterEditOperation() {
        entity.tags.addAll(tagsToPreserve);
    }
}
