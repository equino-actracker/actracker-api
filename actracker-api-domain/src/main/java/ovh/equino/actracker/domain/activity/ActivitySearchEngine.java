package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.EntitySearchResult;

public interface ActivitySearchEngine {

    EntitySearchResult<ActivityDto> findActivities(ActivitySearchCriteria searchCriteria);
}
