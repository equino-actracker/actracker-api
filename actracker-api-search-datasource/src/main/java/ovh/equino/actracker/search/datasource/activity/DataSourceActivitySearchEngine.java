package ovh.equino.actracker.search.datasource.activity;

import ovh.equino.actracker.domain.CommonSearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.activity.ActivityDataSource;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivitySearchCriteria;
import ovh.equino.actracker.domain.activity.ActivitySearchEngine;

import java.util.LinkedList;
import java.util.List;

class DataSourceActivitySearchEngine implements ActivitySearchEngine {

    private final ActivityDataSource activityDataSource;

    DataSourceActivitySearchEngine(ActivityDataSource activityDataSource) {
        this.activityDataSource = activityDataSource;
    }

    @Override
    public EntitySearchResult<ActivityDto> findActivities(ActivitySearchCriteria searchCriteria) {
        var forNextPageIdSearchCriteria = new ActivitySearchCriteria(
                new CommonSearchCriteria(
                        searchCriteria.common().searcher(),
                        searchCriteria.common().pageSize() + 1,   // additional one to calculate next page ID
                        searchCriteria.common().pageId(),
                        searchCriteria.common().sortCriteria()
                ),
                searchCriteria.term(),
                searchCriteria.timeRangeStart(),
                searchCriteria.timeRangeEnd(),
                searchCriteria.excludeFilter(),
                searchCriteria.tags()
        );

        List<ActivityDto> foundActivities = activityDataSource.find(forNextPageIdSearchCriteria);
        String nextPageId = getNextPageId(foundActivities, searchCriteria.common().pageSize());
        List<ActivityDto> results = foundActivities.stream()
                .limit(searchCriteria.common().pageSize())
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
