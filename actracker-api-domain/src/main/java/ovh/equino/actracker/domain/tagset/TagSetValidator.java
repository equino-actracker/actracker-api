package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.EntityValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

class TagSetValidator extends EntityValidator<TagSet> {

    @Override
    protected Class<TagSet> entityType() {
        return TagSet.class;
    }

    @Override
    protected List<String> collectValidationErrors(TagSet tagSet) {
        List<String> validationErrors = new ArrayList<>();

        checkEmptyNameError(tagSet).ifPresent(validationErrors::add);

        return validationErrors;
    }

    private Optional<String> checkEmptyNameError(TagSet tagSet) {
        if (isBlank(tagSet.name())) {
            return Optional.of("Name is empty");
        }
        return Optional.empty();
    }
}
