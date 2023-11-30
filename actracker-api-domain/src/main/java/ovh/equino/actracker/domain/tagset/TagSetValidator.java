package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.EntityValidator;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;

import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

class TagSetValidator extends EntityValidator<TagSet> {

    // TODO validate during TagSet creation before removing verifiers from validator
    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;

    TagSetValidator(TagsAccessibilityVerifier tagsAccessibilityVerifier) {
        this.tagsAccessibilityVerifier = tagsAccessibilityVerifier;
    }

    @Override
    protected Class<TagSet> entityType() {
        return TagSet.class;
    }

    @Override
    protected List<String> collectValidationErrors(TagSet tagSet) {
        List<String> validationErrors = new ArrayList<>();

        checkEmptyNameError(tagSet).ifPresent(validationErrors::add);
        checkNonExistingTagsError(tagSet).ifPresent(validationErrors::add);

        return validationErrors;
    }

    private Optional<String> checkEmptyNameError(TagSet tagSet) {
        if (isBlank(tagSet.name())) {
            return Optional.of("Name is empty");
        }
        return Optional.empty();
    }

    private Optional<String> checkNonExistingTagsError(TagSet tagSet) {
        Set<TagId> notExistingTags = tagsAccessibilityVerifier.nonAccessibleOf(tagSet.tags());
        if (isNotEmpty(notExistingTags)) {
            List<UUID> notExistingTagIds = notExistingTags.stream().map(TagId::id).toList();
            return Optional.of("Selected tags do not exist: %s".formatted(notExistingTagIds));
        }
        return Optional.empty();
    }
}
