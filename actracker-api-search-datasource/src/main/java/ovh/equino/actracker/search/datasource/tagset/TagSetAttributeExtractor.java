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
            return extractTagSetAttribute(attribute, dto);
        }
    }

    private static Optional<Object> extractTagSetAttribute(EntitySortCriteria.Field attribute, TagSetDto dto) {
        if (attribute instanceof TagSetSearchCriteria.SortableField tagSetAttribute) {
            return switch (tagSetAttribute) {
                case NAME -> Optional.of(dto.name());
            };
        }
        return Optional.empty();
    }

    @Override
    public Optional<?> extractIdFrom(TagSetDto dto) {
        return Optional.of(dto.id());
    }

}
