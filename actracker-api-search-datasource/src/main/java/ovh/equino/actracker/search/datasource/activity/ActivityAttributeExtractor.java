package ovh.equino.actracker.search.datasource.activity;

import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivitySearchCriteria;
import ovh.equino.actracker.search.datasource.NextPageIdExtractor;

import java.util.Optional;

class ActivityAttributeExtractor implements NextPageIdExtractor.AttributeValueExtractor<ActivityDto> {

    @Override
    public Optional<?> extractFieldAttribute(EntitySortCriteria.Field attribute, ActivityDto dto) {
        var commonFieldValue = extractCommonAttribute(attribute, dto);
        if (commonFieldValue.isPresent()) {
            return commonFieldValue;
        } else {
            return extractActivityAttribute(attribute, dto);
        }
    }

    @Override
    public Optional<?> extractIdFrom(ActivityDto dto) {
        return Optional.of(dto.id());
    }

    private Optional<Object> extractActivityAttribute(EntitySortCriteria.Field attribute, ActivityDto dto) {
        if (attribute instanceof ActivitySearchCriteria.SortableField activityAttribute) {
            return switch (activityAttribute) {
                case TITLE -> Optional.of(dto.title());
            };
        }
        return Optional.empty();
    }
}
