package ovh.equino.actracker.search.datasource.dashboard;

import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.DashboardSearchCriteria;
import ovh.equino.actracker.search.datasource.NextPageIdExtractor;

import java.util.Optional;

class DashboardAttributeExtractor implements NextPageIdExtractor.AttributeValueExtractor<DashboardDto> {

    @Override
    public Optional<?> extractFieldAttribute(EntitySortCriteria.Field attribute, DashboardDto dto) {
        var commonFieldValue = extractCommonAttribute(attribute, dto);
        if (commonFieldValue.isPresent()) {
            return commonFieldValue;
        } else {
            return extractDashboardAttribute(attribute);
        }
    }

    @Override
    public Optional<?> extractIdFrom(DashboardDto dto) {
        return Optional.of(dto.id());
    }

    private static Optional<?> extractDashboardAttribute(EntitySortCriteria.Field attribute) {
        if (attribute instanceof DashboardSearchCriteria.SortableField dashboardField) {
            return Optional.empty();
        }
        return Optional.empty();
    }
}
