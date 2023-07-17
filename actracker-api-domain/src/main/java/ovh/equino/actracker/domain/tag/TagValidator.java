package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.EntityValidator;

import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

class TagValidator extends EntityValidator<Tag> {

    @Override
    protected Class<Tag> entityType() {
        return Tag.class;
    }

    @Override
    protected List<String> collectValidationErrors(Tag tag) {
        List<String> validationErrors = new LinkedList<>();

        if (isBlank(tag.name())) {
            validationErrors.add("Name is empty");
        }

        return validationErrors;
    }
}
