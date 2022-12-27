package ovh.equino.actracker.domain.activity;

import java.util.List;
import java.util.UUID;

public interface ActivityService {

    ActivityDto createActivity(ActivityDto activity);

    ActivityDto updateActivity(ActivityDto activity);

    List<ActivityDto> getActivities();
}
