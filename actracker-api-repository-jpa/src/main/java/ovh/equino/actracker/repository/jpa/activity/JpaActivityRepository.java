package ovh.equino.actracker.repository.jpa.activity;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityRepository;

import java.util.*;

class JpaActivityRepository implements ActivityRepository {

    private final Map<UUID, ActivityDto> activities = new HashMap<>();

    @Override
    public void addActivity(ActivityDto activity) {
        activities.put(activity.id(), activity);
    }

    @Override
    public void updateActivity(ActivityDto activity) {
        activities.put(activity.id(), activity);
    }

    @Override
    public List<ActivityDto> getActivities() {
        return new ArrayList<>(activities.values());
    }
}
