package ovh.equino.actracker.application.activity;

import ovh.equino.actracker.domain.activity.Activity;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityRepository;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.MetricsExistenceVerifier;
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
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.rename(newTitle, updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto startActivity(Instant startTime, UUID activityId, User updater) {
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.start(startTime, updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto finishActivity(Instant endTime, UUID activityId, User updater) {
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.finish(endTime, updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto updateActivityComment(String newComment, UUID activityId, User updater) {
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.updateComment(newComment, updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto addTagToActivity(UUID tagId, UUID activityId, User updater) {
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.assignTag(new TagId(tagId), updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto removeTagFromActivity(UUID tagId, UUID activityId, User updater) {
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.removeTag(new TagId(tagId), updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public void deleteActivity(UUID activityId, User remover) {
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, remover);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.delete(remover);

        activityRepository.update(activityId, activity.forStorage());
    }
}
