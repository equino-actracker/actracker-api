package ovh.equino.actracker.search.datasource.activity;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.search.datasource.NextPageIdExtractor;

import java.util.Optional;

class ActivityAttributeExtractor implements NextPageIdExtractor.AttributeValueExtractor<ActivityDto> {

    @Override
    public Optional<Object> extractFieldAttribute(String attribute, ActivityDto dto) {
        return switch (attribute) {
            case "id" -> Optional.ofNullable(dto.id());
            default -> Optional.empty();
        };
    }
}
