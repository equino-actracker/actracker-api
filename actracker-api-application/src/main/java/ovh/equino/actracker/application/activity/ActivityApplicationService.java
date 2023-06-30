package ovh.equino.actracker.application.activity;

import ovh.equino.actracker.domain.activity.Activity;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityRepository;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.UUID;

public class ActivityApplicationService {

    private final ActivityRepository activityRepository;
    private final TagRepository tagRepository;

    ActivityApplicationService(ActivityRepository activityRepository, TagRepository tagRepository) {
        this.activityRepository = activityRepository;
        this.tagRepository = tagRepository;
    }

    public ActivityDto renameActivity(String newTitle, UUID activityId, User updater) {
        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, new TagsExistenceVerifier(tagRepository, updater));
        activity.rename(newTitle, updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto startActivity(Instant startTime, UUID activityId, User updater) {
        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, new TagsExistenceVerifier(tagRepository, updater));
        activity.start(startTime, updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto finishActivity(Instant endTime, UUID activityId, User updater) {
        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, new TagsExistenceVerifier(tagRepository, updater));
        activity.finish(endTime, updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto updateActivityComment(String newComment, UUID activityId, User updater) {
        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, new TagsExistenceVerifier(tagRepository, updater));
        activity.updateComment(newComment, updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto addTagToActivity(UUID tagId, UUID activityId, User updater) {
        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, new TagsExistenceVerifier(tagRepository, updater));
        activity.assignTag(new TagId(tagId), updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto removeTagFromActivity(UUID tagId, UUID activityId, User updater) {
        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, new TagsExistenceVerifier(tagRepository, updater));
        activity.removeTag(new TagId(tagId), updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public void deleteActivity(UUID activityId, User remover) {
        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, new TagsExistenceVerifier(tagRepository, remover));
        activity.delete(remover);

        activityRepository.update(activityId, activity.forStorage());
    }
}
