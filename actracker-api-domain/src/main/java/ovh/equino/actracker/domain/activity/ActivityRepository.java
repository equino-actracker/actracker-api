package ovh.equino.actracker.domain.activity;

import java.util.List;

public interface ActivityRepository {

    void addActivity(ActivityDto activity);

    void updateActivity(ActivityDto activity);

    List<ActivityDto> getActivities();
}
