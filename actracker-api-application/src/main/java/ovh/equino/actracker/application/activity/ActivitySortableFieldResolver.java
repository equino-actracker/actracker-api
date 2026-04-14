package ovh.equino.actracker.application.activity;

import ovh.equino.actracker.application.SortCriteria;
import ovh.equino.actracker.domain.activity.ActivitySearchCriteria;

import java.util.Optional;

import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.isBlank;

class ActivitySortableFieldResolver implements SortCriteria.FieldResolver {

    @Override
    public Optional<ActivitySearchCriteria.SortableField> fromString(String fieldName) {
        if (isBlank(fieldName)) {
            return Optional.empty();
        }
        return stream(ActivitySearchCriteria.SortableField.values())
                .filter(it -> it.toString().equals(fieldName.toUpperCase()))
                .findAny();
    }
}
