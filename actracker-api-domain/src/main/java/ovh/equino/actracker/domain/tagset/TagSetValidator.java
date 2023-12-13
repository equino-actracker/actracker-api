package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.EntityValidator;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

class TagSetValidator extends EntityValidator<TagSet> {

    @Override
    protected Class<TagSet> entityType() {
        return TagSet.class;
    }

    @Override
    protected List<String> collectValidationErrors(TagSet tagSet) {
        List<String> validationErrors = new ArrayList<>();

        if (isBlank(tagSet.name())) {
            validationErrors.add("Name is empty");
        }

        return validationErrors;
    }

}
