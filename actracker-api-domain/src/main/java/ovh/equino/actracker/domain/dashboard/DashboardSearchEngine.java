package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;

public interface DashboardSearchEngine {

    EntitySearchResult<DashboardDto> findDashboards(EntitySearchCriteria searchCriteria);
}
