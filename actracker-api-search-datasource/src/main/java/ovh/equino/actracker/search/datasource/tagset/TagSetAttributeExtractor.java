package ovh.equino.actracker.search.datasource.tagset;

import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.search.datasource.NextPageIdExtractor;

import java.util.Optional;

class TagSetAttributeExtractor implements NextPageIdExtractor.AttributeValueExtractor<TagSetDto> {

    @Override
    public Optional<Object> extractFieldAttribute(String attribute, TagSetDto dto) {
        return switch (attribute) {
            case "id" -> Optional.ofNullable(dto.id());
            default -> Optional.empty();
        };
    }
}
