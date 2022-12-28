package ovh.equino.actracker.domain.activity;

import java.util.List;

class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;

    ActivityServiceImpl(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public ActivityDto createActivity(ActivityDto activity) {
        Activity newActivity = new Activity(activity);
        activityRepository.addActivity(newActivity.toDto());
        return newActivity.toDto();
    }

    @Override
    public ActivityDto updateActivity(ActivityDto activity) {
        activityRepository.updateActivity(activity);
        return activity;
    }

    @Override
    public List<ActivityDto> getActivities() {
        return activityRepository.getActivities();
    }
}
