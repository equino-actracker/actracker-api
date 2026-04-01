package ovh.equino.actracker.search.datasource.tag;

import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.search.datasource.NextPageIdExtractor;

import java.util.Optional;

class TagAttributeExtractor implements NextPageIdExtractor.AttributeValueExtractor<TagDto> {

    @Override
    public Optional<Object> extractFieldAttribute(String attribute, TagDto dto) {
        return switch (attribute) {
            case "id" -> Optional.ofNullable(dto.id());
            default -> Optional.empty();
        };
    }
}
