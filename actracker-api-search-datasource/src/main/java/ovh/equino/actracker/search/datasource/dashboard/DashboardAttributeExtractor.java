package ovh.equino.actracker.search.datasource.dashboard;

import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.DashboardSearchCriteria;
import ovh.equino.actracker.search.datasource.NextPageIdExtractor;

import java.util.Optional;

final class DashboardAttributeExtractor extends NextPageIdExtractor.AttributeValueExtractor<DashboardDto> {

    @Override
    protected Optional<?> extractEntityAttribute(EntitySortCriteria.Field attribute, DashboardDto dto) {
        if (attribute instanceof DashboardSearchCriteria.SortableField dashboardAttribute) {
            return switch (dashboardAttribute) {
                case NAME -> Optional.ofNullable(dto.name());
            };
        }
        return Optional.empty();
    }

    @Override
    public Optional<?> extractIdFrom(DashboardDto dto) {
        return Optional.of(dto.id());
    }
}
