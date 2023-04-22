package ovh.equino.actracker.rest.spring.dashboard;

import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.rest.spring.PayloadMapper;
import ovh.equino.actracker.rest.spring.SearchResponse;

import java.util.List;

class DashboardMapper extends PayloadMapper {

    DashboardDto fromRequest(Dashboard dashboardRequest) {
        return new DashboardDto(
                dashboardRequest.name()
        );
    }

    Dashboard toResponse(DashboardDto dashboard) {
        return new Dashboard(
                uuidToString(dashboard.id()),
                dashboard.name()
        );
    }

    SearchResponse<Dashboard> toResponse(EntitySearchResult<DashboardDto> dashboardSearchResult) {
        List<Dashboard> foundDashboards = toResponse(dashboardSearchResult.results());
        return new SearchResponse<>(dashboardSearchResult.nextPageId(), foundDashboards);
    }

    private List<Dashboard> toResponse(List<DashboardDto> dashboards) {
        return dashboards.stream()
                .map(this::toResponse)
                .toList();
    }
}
