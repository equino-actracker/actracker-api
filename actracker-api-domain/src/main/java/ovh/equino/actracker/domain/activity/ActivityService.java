package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.UUID;

public interface ActivityService {

    ActivityDto createActivity(ActivityDto newActivityData, User creator);

    ActivityDto updateActivity(UUID activityId, ActivityDto updatedActivityData, User updater);

    // TODO delete
    List<ActivityDto> getActivities(User searcher);

    EntitySearchResult<ActivityDto> searchActivities(EntitySearchCriteria searchCriteria);

    void deleteActivity(UUID activityId, User remover);
}
