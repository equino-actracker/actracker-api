package ovh.equino.actracker.search.datasource.activity;

import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivitySearchCriteria;
import ovh.equino.actracker.search.datasource.NextPageIdExtractor;

import java.util.Optional;

final class ActivityAttributeExtractor extends NextPageIdExtractor.AttributeValueExtractor<ActivityDto> {

    @Override
    protected Optional<?> extractEntityAttribute(EntitySortCriteria.Field attribute, ActivityDto dto) {
        if (attribute instanceof ActivitySearchCriteria.SortableField activityAttribute) {
            return switch (activityAttribute) {
                case TITLE -> Optional.ofNullable(dto.title());
                case END_TIME -> Optional.ofNullable(dto.endTime());
            };
        }
        return Optional.empty();
    }

    @Override
    public Optional<?> extractIdFrom(ActivityDto dto) {
        return Optional.of(dto.id());
    }
}
