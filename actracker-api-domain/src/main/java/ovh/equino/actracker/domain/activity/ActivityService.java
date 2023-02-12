package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.activity.error.ActivityNotFoundException;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.UUID;

public class ActivityService {

    private final ActivityRepository activityRepository;

    ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public ActivityDto createActivity(ActivityDto newActivityData, User creator) {
        Activity createdActivity = new Activity(new ActivityId(), newActivityData, creator);
        activityRepository.add(createdActivity.toDto());
        return createdActivity.toDto();
    }

    public ActivityDto updateActivity(UUID activityId, ActivityDto updatedActivityData, User updater) {
        ActivityDto existingActivityData = activityRepository.findById(activityId)
                .orElseThrow(ActivityNotFoundException::new);

        Activity activity = new Activity(existingActivityData);

        if (activity.isNotAvailableFor(updater)) {
            throw new ActivityNotFoundException();
        }

        activity.updateTo(updatedActivityData);
        activityRepository.udpate(activityId, activity.toDto());
        return activity.toDto();
    }

    public List<ActivityDto> getActivities(User searcher) {
        return activityRepository.findAll(searcher);
    }
}
