package ovh.equino.actracker.repository.jpa.activity;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityRepository;

import java.util.*;

class JpaActivityRepository implements ActivityRepository {

    private final Map<UUID, ActivityDto> activities = new HashMap<>();

    @Override
    public void add(ActivityDto activity) {
        activities.put(activity.id(), activity);
    }

    @Override
    public void udpate(UUID activityId, ActivityDto activity) {
        activities.put(activity.id(), activity);
    }

    @Override
    public Optional<ActivityDto> findById(UUID activityId) {
        return Optional.ofNullable(activities.get(activityId));
    }

    @Override
    public List<ActivityDto> findAll() {
        return activities.values().stream().toList();
    }
}
