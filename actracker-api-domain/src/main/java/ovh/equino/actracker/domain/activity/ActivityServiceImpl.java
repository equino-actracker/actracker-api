package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.UUID;

class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityNotifier activityNotifier;

    ActivityServiceImpl(ActivityRepository activityRepository, ActivityNotifier activityNotifier) {
        this.activityRepository = activityRepository;
        this.activityNotifier = activityNotifier;
    }

    @Override
    public ActivityDto createActivity(ActivityDto newActivityData, User creator) {

        Activity createdActivity = new Activity(new ActivityId(), newActivityData, creator);

        ActivityDto activityDto = createdActivity.toDto();
        activityRepository.add(activityDto);
        activityNotifier.notifyChanged(createdActivity.toChangeNotification());

        return activityDto;
    }

    @Override
    public ActivityDto updateActivity(UUID activityId, ActivityDto updatedActivityData, User updater) {

        Activity activity = getActivityIfAuthorized(updater, activityId);
        activity.updateTo(updatedActivityData);

        ActivityDto activityDto = activity.toDto();
        activityRepository.update(activityId, activityDto);
        activityNotifier.notifyChanged(activity.toChangeNotification());

        return activityDto;
    }

    @Override
    public List<ActivityDto> getActivities(User searcher) {
        return activityRepository.findAll(searcher);
    }

    @Override
    public void deleteActivity(UUID activityId, User remover) {

        Activity activity = getActivityIfAuthorized(remover, activityId);
        activity.delete();

        activityRepository.update(activityId, activity.toDto());
        activityNotifier.notifyChanged(activity.toChangeNotification());
    }

    private Activity getActivityIfAuthorized(User user, UUID activityId) {
        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromDto(activityDto);

        if (activity.isNotAvailableFor(user)) {
            throw new EntityNotFoundException(Activity.class, activityId);
        }
        return activity;
    }
}
