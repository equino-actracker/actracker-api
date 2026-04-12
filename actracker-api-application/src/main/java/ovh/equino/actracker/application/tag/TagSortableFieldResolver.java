package ovh.equino.actracker.application.tag;

import ovh.equino.actracker.application.SortCriteria;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;

import java.util.Optional;

import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.isBlank;

class TagSortableFieldResolver implements SortCriteria.FieldResolver {

    @Override
    public Optional<? extends EntitySortCriteria.Field> fromString(String fieldName) {
        if (isBlank(fieldName)) {
            return Optional.empty();
        }
        var commonField = commonFieldFromString(fieldName);
        if (commonField.isPresent()) {
            return commonField;
        }
        return tagSortableField(fieldName);
    }

    private static Optional<TagSearchCriteria.SortableField> tagSortableField(String fieldName) {
        return stream(TagSearchCriteria.SortableField.values())
                .filter(it -> it.toString().equals(fieldName.toUpperCase()))
                .findAny();
    }
}
