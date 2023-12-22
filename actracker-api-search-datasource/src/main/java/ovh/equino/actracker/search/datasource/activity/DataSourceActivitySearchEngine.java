package ovh.equino.actracker.search.datasource.activity;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.activity.ActivityDataSource;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivitySearchEngine;

import java.util.LinkedList;
import java.util.List;

class DataSourceActivitySearchEngine implements ActivitySearchEngine {

    private final ActivityDataSource activityDataSource;

    DataSourceActivitySearchEngine(ActivityDataSource activityDataSource) {
        this.activityDataSource = activityDataSource;
    }

    @Override
    public EntitySearchResult<ActivityDto> findActivities(EntitySearchCriteria searchCriteria) {
        EntitySearchCriteria forNextPageIdSearchCriteria = new EntitySearchCriteria(
                searchCriteria.searcher(),
                searchCriteria.pageSize() + 1,   // additional one to calculate next page ID
                searchCriteria.pageId(),
                searchCriteria.term(),
                searchCriteria.timeRangeStart(),
                searchCriteria.timeRangeEnd(),
                searchCriteria.excludeFilter(),
                searchCriteria.tags()
        );

        List<ActivityDto> foundActivities = activityDataSource.find(forNextPageIdSearchCriteria);
        String nextPageId = getNextPageId(foundActivities, searchCriteria.pageSize());
        List<ActivityDto> results = foundActivities.stream()
                .limit(searchCriteria.pageSize())
                .toList();

        return new EntitySearchResult<>(nextPageId, results);
    }

    private String getNextPageId(List<ActivityDto> foundActivities, int pageSize) {
        if (foundActivities.size() <= pageSize) {
            return null;
        }
        ActivityDto lastActivity = new LinkedList<>(foundActivities).get(pageSize);
        return lastActivity.id().toString();
    }
}
