package ovh.equino.actracker.search.repository.dashboard;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.DashboardRepository;
import ovh.equino.actracker.domain.dashboard.DashboardSearchEngine;

import java.util.LinkedList;
import java.util.List;

class RepositoryDashboardSearchEngine implements DashboardSearchEngine {

    private final DashboardRepository dashboardRepository;

    RepositoryDashboardSearchEngine(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    public EntitySearchResult<DashboardDto> findDashboards(EntitySearchCriteria searchCriteria) {
        EntitySearchCriteria forNextPageIdSearchCriteria = new EntitySearchCriteria(
                searchCriteria.searcher(),
                searchCriteria.pageSize() + 1,   // additional one to calculate next page ID
                searchCriteria.pageId(),
                searchCriteria.term(),
                searchCriteria.timeRangeStart(),
                searchCriteria.timeRangeEnd(),
                searchCriteria.excludeFilter(),
                searchCriteria.sortCriteria()
        );

        List<DashboardDto> foundDashboards = dashboardRepository.find(forNextPageIdSearchCriteria);
        String nextPageId = getNextPageId(foundDashboards, searchCriteria.pageSize());
        List<DashboardDto> results = foundDashboards.stream()
                .limit(searchCriteria.pageSize())
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
