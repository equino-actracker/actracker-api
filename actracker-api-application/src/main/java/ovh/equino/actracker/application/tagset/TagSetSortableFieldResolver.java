package ovh.equino.actracker.application.tagset;

import ovh.equino.actracker.application.SortCriteria;
import ovh.equino.actracker.domain.tagset.TagSetSearchCriteria;

import java.util.Optional;

import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.isBlank;

class TagSetSortableFieldResolver implements SortCriteria.FieldResolver {

    @Override
    public Optional<TagSetSearchCriteria.SortableField> fromString(String fieldName) {
        if (isBlank(fieldName)) {
            return Optional.empty();
        }
        return stream(TagSetSearchCriteria.SortableField.values())
                .filter(it -> it.toString().equals(fieldName.toUpperCase()))
                .findAny();
    }
}
