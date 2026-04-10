package ovh.equino.actracker.search.datasource.dashboard;

import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.search.datasource.NextPageIdExtractor;

import java.util.Optional;

class DashboardAttributeExtractor implements NextPageIdExtractor.AttributeValueExtractor<DashboardDto> {

    @Override
    public Optional<Object> extractFieldAttribute(String attribute, DashboardDto dto) {
        return switch (attribute) {
            case "id" -> Optional.ofNullable(dto.id());
            default -> Optional.empty();
        };
    }
}
