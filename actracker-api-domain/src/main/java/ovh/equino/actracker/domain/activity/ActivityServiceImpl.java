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
    public ActivityDto createActivity(ActivityDto newActivityData) {
        Activity createdActivity = new Activity(new ActivityId(), newActivityData);
        activityRepository.add(createdActivity.toDto());
        return createdActivity.toDto();
    }

    @Override
    public ActivityDto updateActivity(UUID activityId, ActivityDto updatedActivityData) {
        ActivityDto foundActivity = activityRepository.findById(activityId)
                .orElseThrow(ActivityNotFoundException::new);
        Activity existingActivity = new Activity(foundActivity);
        existingActivity.updateTo(updatedActivityData);
        activityRepository.udpate(activityId, existingActivity.toDto());
        return existingActivity.toDto();
    }

    @Override
    public List<ActivityDto> getActivities() {
        return activityRepository.findAll();
    }
}
