package ovh.equino.actracker.domain.activity;

import java.util.List;
import java.util.UUID;

public interface ActivityService {

    ActivityDto createActivity(ActivityDto newActivityData);

    ActivityDto updateActivity(UUID activityId, ActivityDto updatedActivityData);

    List<ActivityDto> getActivities();
}
