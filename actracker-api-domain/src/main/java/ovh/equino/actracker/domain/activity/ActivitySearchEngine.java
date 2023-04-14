package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;

public interface ActivitySearchEngine {

    EntitySearchResult<ActivityDto> findActivities(EntitySearchCriteria searchCriteria);
}
