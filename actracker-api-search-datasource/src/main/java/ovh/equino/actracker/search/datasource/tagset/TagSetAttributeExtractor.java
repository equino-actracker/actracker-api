package ovh.equino.actracker.search.datasource.tagset;

import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetSearchCriteria;
import ovh.equino.actracker.search.datasource.NextPageIdExtractor;

import java.util.Optional;

class TagSetAttributeExtractor implements NextPageIdExtractor.AttributeValueExtractor<TagSetDto> {

    @Override
    public Optional<?> extractFieldAttribute(EntitySortCriteria.Field attribute, TagSetDto dto) {
        var commonFieldValue = extractCommonAttribute(attribute, dto);
        if (commonFieldValue.isPresent()) {
            return commonFieldValue;
        } else {
            return extractTagSetAttribute(attribute);
        }
    }

    private static Optional<Object> extractTagSetAttribute(EntitySortCriteria.Field attribute) {
        if (attribute instanceof TagSetSearchCriteria.SortableField tagSetField) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public Optional<?> extractIdFrom(TagSetDto dto) {
        return Optional.of(dto.id());
    }

}
