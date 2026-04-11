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
            return extractActivityAttribute(attribute);
        }
    }

    @Override
    public Optional<?> extractIdFrom(ActivityDto dto) {
        return Optional.of(dto.id());
    }

    private static Optional<Object> extractActivityAttribute(EntitySortCriteria.Field attribute) {
        if (attribute instanceof ActivitySearchCriteria.SortableField activityField) {
            return Optional.empty();
        }
        return Optional.empty();
    }
}
