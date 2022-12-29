package ovh.equino.actracker.domain.activity;

import java.util.List;
import java.util.UUID;

public interface ActivityService {

    ActivityDto createActivity(ActivityDto activity);

    ActivityDto updateActivity(UUID activityId, ActivityDto activity);

    List<ActivityDto> getActivities();
}
