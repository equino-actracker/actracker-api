package ovh.equino.actracker.search.datasource.activity;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.activity.ActivityDataSource;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivitySearchCriteria;
import ovh.equino.actracker.domain.activity.ActivitySearchEngine;
import ovh.equino.actracker.search.datasource.DataSourceSearchEngine;

import java.util.List;

class DataSourceActivitySearchEngine
        extends DataSourceSearchEngine<ActivityDto, ActivitySearchCriteria>
        implements ActivitySearchEngine {

    private final ActivityDataSource activityDataSource;

    DataSourceActivitySearchEngine(ActivityDataSource activityDataSource) {
        super(new ActivityAttributeExtractor());
        this.activityDataSource = activityDataSource;
    }

    @Override
    public EntitySearchResult<ActivityDto> findActivities(ActivitySearchCriteria searchCriteria) {
        return findBy(searchCriteria);
    }

    @Override
    protected List<ActivityDto> searchInDataSource(ActivitySearchCriteria searchCriteria) {
        return activityDataSource.find(searchCriteria);
    }

    @Override
    protected ActivitySearchCriteria withCommonCriteriaReplaced(ActivitySearchCriteria searchCriteria,
                                                                EntitySearchCriteria.Common newCommonCriteria) {
        return new ActivitySearchCriteria(
                newCommonCriteria,
                searchCriteria.term(),
                searchCriteria.timeRangeStart(),
                searchCriteria.timeRangeEnd(),
                searchCriteria.excludeFilter(),
                searchCriteria.tags()
        );
    }
}
