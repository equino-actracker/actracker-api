package ovh.equino.actracker.search.datasource.dashboard;

import ovh.equino.actracker.domain.CommonSearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.dashboard.DashboardDataSource;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.DashboardSearchCriteria;
import ovh.equino.actracker.domain.dashboard.DashboardSearchEngine;

import java.util.LinkedList;
import java.util.List;

class DataSourceDashboardSearchEngine implements DashboardSearchEngine {

    private final DashboardDataSource dashboardDataSource;

    DataSourceDashboardSearchEngine(DashboardDataSource dashboardDataSource) {
        this.dashboardDataSource = dashboardDataSource;
    }

    @Override
    public EntitySearchResult<DashboardDto> findDashboards(DashboardSearchCriteria searchCriteria) {
        var forNextPageIdSearchCriteria = new DashboardSearchCriteria(
                new CommonSearchCriteria(
                        searchCriteria.common().searcher(),
                        searchCriteria.common().pageSize() + 1,   // additional one to calculate next page ID
                        searchCriteria.common().pageId()
                ),
                searchCriteria.term(),
                searchCriteria.timeRangeStart(),
                searchCriteria.timeRangeEnd(),
                searchCriteria.excludeFilter(),
                searchCriteria.tags()
        );

        List<DashboardDto> foundDashboards = dashboardDataSource.find(forNextPageIdSearchCriteria);
        String nextPageId = getNextPageId(foundDashboards, searchCriteria.common().pageSize());
        List<DashboardDto> results = foundDashboards.stream()
                .limit(searchCriteria.common().pageSize())
                .toList();

        return new EntitySearchResult<>(nextPageId, results);
    }

    private String getNextPageId(List<DashboardDto> foundDashboards, int pageSize) {
        if (foundDashboards.size() <= pageSize) {
            return null;
        }
        DashboardDto lastDashboard = new LinkedList<>(foundDashboards).get(pageSize);
        return lastDashboard.id().toString();
    }
}
