package ovh.equino.actracker.search.repository.activity;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityRepository;
import ovh.equino.actracker.domain.activity.ActivitySearchEngine;

import java.util.LinkedList;
import java.util.List;

class RepositoryActivitySearchEngine implements ActivitySearchEngine {

    private final ActivityRepository activityRepository;

    RepositoryActivitySearchEngine(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
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
                searchCriteria.tags(),
                searchCriteria.sortCriteria()
        );

        List<ActivityDto> foundActivities = activityRepository.find(forNextPageIdSearchCriteria);
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
