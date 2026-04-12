package ovh.equino.actracker.application.activity;

import ovh.equino.actracker.application.SortCriteria;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.activity.ActivitySearchCriteria;

import java.util.Optional;

import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.isBlank;

class ActivitySortableFieldResolver implements SortCriteria.FieldResolver {

    @Override
    public Optional<? extends EntitySortCriteria.Field> fromString(String fieldName) {
        if (isBlank(fieldName)) {
            return Optional.empty();
        }
        var commonField = commonFieldFromString(fieldName);
        if (commonField.isPresent()) {
            return commonField;
        }
        return activitySortableField(fieldName);
    }

    private static Optional<ActivitySearchCriteria.SortableField> activitySortableField(String fieldName) {
        return stream(ActivitySearchCriteria.SortableField.values())
                .filter(it -> it.toString().equals(fieldName.toUpperCase()))
                .findAny();
    }
}
