package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.activity.error.ActivityNotFoundException;

import java.util.List;
import java.util.UUID;

class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;

    ActivityServiceImpl(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public ActivityDto createActivity(ActivityDto activity) {
        Activity createdActivity = Activity.createdFrom(activity);
        activityRepository.add(createdActivity.toDto());
        return createdActivity.toDto();
    }

    @Override
    public ActivityDto updateActivity(UUID activityId, ActivityDto activity) {
        ActivityDto foundActivity = activityRepository.findById(activityId)
                .orElseThrow(ActivityNotFoundException::new);
        Activity existingActivity = Activity.existingFrom(foundActivity);
        existingActivity.updateTo(activity);
        activityRepository.udpate(activityId, existingActivity.toDto());
        return existingActivity.toDto();
    }

    @Override
    public List<ActivityDto> getActivities() {
        return activityRepository.findAll();
    }
}
