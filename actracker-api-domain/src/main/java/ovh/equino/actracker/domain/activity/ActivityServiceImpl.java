package ovh.equino.actracker.domain.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.isNull;

class ActivityServiceImpl implements ActivityService {

    private final Map<UUID, Activity> activities = new HashMap<>();

    @Override
    public ActivityDto createActivity(ActivityDto activity) {
        Activity newActivity = Activity.newFrom(activity);
        activities.put(newActivity.getId(), newActivity);
        return newActivity.toDto();
    }

    @Override
    public ActivityDto updateActivity(ActivityDto activity) {
        Activity foundActivity = activities.get(activity.id());
        if (isNull(foundActivity)) {
            throw new IllegalArgumentException();
        }
        foundActivity.updateTo(activity);
        return foundActivity.toDto();
    }

    @Override
    public List<ActivityDto> getActivities() {
        return activities.values().stream()
                .map(Activity::toDto)
                .toList();
    }
}
