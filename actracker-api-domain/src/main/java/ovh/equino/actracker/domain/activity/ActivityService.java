package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.activity.error.ActivityNotFoundException;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.UUID;

public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityNotifier activityNotifier;

    ActivityService(ActivityRepository activityRepository, ActivityNotifier activityNotifier) {
        this.activityRepository = activityRepository;
        this.activityNotifier = activityNotifier;
    }

    public ActivityDto createActivity(ActivityDto newActivityData, User creator) {
        Activity createdActivity = new Activity(new ActivityId(), newActivityData, creator);

        ActivityDto activityDto = createdActivity.toDto();
        activityRepository.add(activityDto);
        activityNotifier.notifyChanged(activityDto);

        return activityDto;
    }

    public ActivityDto updateActivity(UUID activityId, ActivityDto updatedActivityData, User updater) {
        ActivityDto existingActivityData = activityRepository.findById(activityId)
                .orElseThrow(ActivityNotFoundException::new);

        Activity activity = new Activity(existingActivityData);

        if (activity.isNotAvailableFor(updater)) {
            throw new ActivityNotFoundException();
        }

        activity.updateTo(updatedActivityData);

        ActivityDto activityDto = activity.toDto();
        activityRepository.update(activityId, activityDto);
        activityNotifier.notifyChanged(activityDto);

        return activityDto;
    }

    public List<ActivityDto> getActivities(User searcher) {
        return activityRepository.findAll(searcher);
    }
}
