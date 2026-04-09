package ovh.equino.actracker.search.datasource.dashboard;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.dashboard.DashboardDataSource;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.DashboardSearchCriteria;
import ovh.equino.actracker.domain.dashboard.DashboardSearchEngine;
import ovh.equino.actracker.search.datasource.DataSourceSearchEngine;

import java.util.List;

class DataSourceDashboardSearchEngine
        extends DataSourceSearchEngine<DashboardDto, DashboardSearchCriteria>
        implements DashboardSearchEngine {

    private final DashboardDataSource dashboardDataSource;

    DataSourceDashboardSearchEngine(DashboardDataSource dashboardDataSource) {
        super(new DashboardAttributeExtractor());
        this.dashboardDataSource = dashboardDataSource;
    }

    @Override
    public EntitySearchResult<DashboardDto> findDashboards(DashboardSearchCriteria searchCriteria) {
        return findBy(searchCriteria);
    }

    @Override
    protected List<DashboardDto> searchInDataSource(DashboardSearchCriteria searchCriteria) {
        return dashboardDataSource.find(searchCriteria);
    }

    @Override
    protected DashboardSearchCriteria withCommonCriteriaReplaced(DashboardSearchCriteria searchCriteria,
                                                                 EntitySearchCriteria.Common newCommonCriteria) {
        return new DashboardSearchCriteria(
                newCommonCriteria,
                searchCriteria.term(),
                searchCriteria.excludeFilter()
        );
    }
}
