package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.user.User;

import java.util.UUID;

public interface ActivityService {

    ActivityDto createActivity(ActivityDto newActivityData, User creator);

    ActivityDto updateActivity(UUID activityId, ActivityDto updatedActivityData, User updater);

    EntitySearchResult<ActivityDto> searchActivities(EntitySearchCriteria searchCriteria);

    void deleteActivity(UUID activityId, User remover);

    ActivityDto switchToNewActivity(ActivityDto activityToSwitch, User switcher);
}
