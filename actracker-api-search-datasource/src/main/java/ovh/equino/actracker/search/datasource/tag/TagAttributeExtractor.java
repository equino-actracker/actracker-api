package ovh.equino.actracker.search.datasource.tag;

import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;
import ovh.equino.actracker.search.datasource.NextPageIdExtractor;

import java.util.Optional;

final class TagAttributeExtractor extends NextPageIdExtractor.AttributeValueExtractor<TagDto> {

    @Override
    protected Optional<?> extractEntityAttribute(EntitySortCriteria.Field attribute, TagDto dto) {
        if (attribute instanceof TagSearchCriteria.SortableField tagAttribute) {
            return switch (tagAttribute) {
                case NAME -> Optional.ofNullable(dto.name());
            };
        }
        return Optional.empty();
    }

    @Override
    public Optional<?> extractIdFrom(TagDto dto) {
        return Optional.of(dto.id());
    }
}
