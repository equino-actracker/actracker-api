package ovh.equino.actracker.application.dashboard;

import ovh.equino.actracker.application.SortCriteria;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.dashboard.DashboardSearchCriteria;

import java.util.Optional;

import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.isBlank;

class DashboardSortableFieldResolver implements SortCriteria.FieldResolver {

    @Override
    public Optional<? extends EntitySortCriteria.Field> fromString(String fieldName) {
        if (isBlank(fieldName)) {
            return Optional.empty();
        }
        var commonField = commonFieldFromString(fieldName);
        if (commonField.isPresent()) {
            return commonField;
        }
        return dashboardSortableField(fieldName);
    }

    private static Optional<DashboardSearchCriteria.SortableField> dashboardSortableField(String fieldName) {
        return stream(DashboardSearchCriteria.SortableField.values())
                .filter(it -> it.toString().equals(fieldName.toUpperCase()))
                .findAny();
    }
}
