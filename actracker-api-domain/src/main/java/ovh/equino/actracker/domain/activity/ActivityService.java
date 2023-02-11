package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.UUID;

public interface ActivityService {

    ActivityDto createActivity(ActivityDto newActivityData, User creator);

    ActivityDto updateActivity(UUID activityId, ActivityDto updatedActivityData, User updater);

    List<ActivityDto> getActivities(User searcher);
}
