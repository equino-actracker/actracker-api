package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.EntityValidator;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

class TagSetValidator extends EntityValidator<TagSet> {

    private final TagSet tagSet;
    private final TagsExistenceVerifier tagsExistenceVerifier;

    public TagSetValidator(TagSet tagSet, TagsExistenceVerifier tagsExistenceVerifier) {
        this.tagSet = tagSet;
        this.tagsExistenceVerifier = tagsExistenceVerifier;
    }

    @Override
    protected List<String> collectValidationErrors() {
        List<String> validationErrors = new ArrayList<>();

        Set<TagId> notExistingTags = tagsExistenceVerifier.notExisting(tagSet.tags());
        if (isNotEmpty(notExistingTags)) {
            List<UUID> notExistingTagIds = notExistingTags.stream().map(TagId::id).toList();
            validationErrors.add("Selected tags do not exist: %s".formatted(notExistingTagIds));
        }

        if (isBlank(tagSet.name())) {
            validationErrors.add("Name is empty");
        }

        return validationErrors;
    }

    @Override
    protected Class<TagSet> entityType() {
        return TagSet.class;
    }
}
