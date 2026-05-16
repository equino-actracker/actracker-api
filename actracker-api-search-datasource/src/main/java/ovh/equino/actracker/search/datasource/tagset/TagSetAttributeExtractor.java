package ovh.equino.actracker.search.datasource.tagset;

import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetSearchCriteria;
import ovh.equino.actracker.search.datasource.NextPageIdExtractor;

import java.util.Optional;

final class TagSetAttributeExtractor extends NextPageIdExtractor.AttributeValueExtractor<TagSetDto> {

    @Override
    protected Optional<?> extractEntityAttribute(EntitySortCriteria.Field attribute, TagSetDto dto) {
        if (attribute instanceof TagSetSearchCriteria.SortableField tagSetAttribute) {
            return switch (tagSetAttribute) {
                case NAME -> Optional.ofNullable(dto.name());
            };
        }
        return Optional.empty();
    }

    @Override
    public Optional<?> extractIdFrom(TagSetDto dto) {
        return Optional.of(dto.id());
    }

}
