package ovh.equino.actracker.search.datasource.tag;

import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;
import ovh.equino.actracker.search.datasource.NextPageIdExtractor;

import java.util.Optional;

class TagAttributeExtractor implements NextPageIdExtractor.AttributeValueExtractor<TagDto> {

    @Override
    public Optional<?> extractFieldAttribute(EntitySortCriteria.Field attribute, TagDto dto) {
        var commonFieldValue = extractCommonAttribute(attribute, dto);
        if (commonFieldValue.isPresent()) {
            return commonFieldValue;
        } else {
            return extractTagAttribute(attribute, dto);
        }
    }

    @Override
    public Optional<?> extractIdFrom(TagDto dto) {
        return Optional.of(dto.id());
    }

    private static Optional<Object> extractTagAttribute(EntitySortCriteria.Field attribute, TagDto dto) {
        if (attribute instanceof TagSearchCriteria.SortableField tagAttribute) {
            return switch (tagAttribute) {
                case NAME -> Optional.of(dto.name());
            };
        }
        return Optional.empty();
    }
}
