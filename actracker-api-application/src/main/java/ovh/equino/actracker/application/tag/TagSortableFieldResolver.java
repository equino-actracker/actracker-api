package ovh.equino.actracker.application.tag;

import ovh.equino.actracker.application.SortCriteria;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;

import java.util.Optional;

import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.isBlank;

class TagSortableFieldResolver implements SortCriteria.FieldResolver {

    @Override
    public Optional<TagSearchCriteria.SortableField> fromString(String fieldName) {
        if (isBlank(fieldName)) {
            return Optional.empty();
        }
        return stream(TagSearchCriteria.SortableField.values())
                .filter(it -> it.toString().equals(fieldName.toUpperCase()))
                .findAny();
    }
}
