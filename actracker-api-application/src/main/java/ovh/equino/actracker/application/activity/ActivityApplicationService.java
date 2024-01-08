package ovh.equino.actracker.application.activity;

import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.activity.*;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.MetricId;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.Instant.now;

public class ActivityApplicationService {

    private final ActivityFactory activityFactory;
    private final ActivityRepository activityRepository;
    private final ActivityDataSource activityDataSource;
    private final ActivitySearchEngine activitySearchEngine;
    private final ActivityNotifier activityNotifier;
    private final ActorExtractor actorExtractor;

    public ActivityApplicationService(ActivityFactory activityFactory,
                                      ActivityRepository activityRepository,
                                      ActivityDataSource activityDataSource,
                                      ActivitySearchEngine activitySearchEngine,
                                      ActivityNotifier activityNotifier,
                                      ActorExtractor actorExtractor) {

        this.activityFactory = activityFactory;
        this.activityRepository = activityRepository;
        this.activityDataSource = activityDataSource;
        this.activitySearchEngine = activitySearchEngine;
        this.activityNotifier = activityNotifier;
        this.actorExtractor = actorExtractor;
    }

    public ActivityResult getActivity(UUID activityId) {
        return findActivityResult(new ActivityId(activityId))
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));
    }

    private Optional<ActivityResult> findActivityResult(ActivityId activityId) {
        User actor = actorExtractor.getActor();
        return activityDataSource.find(activityId, actor).map(this::toActivityResult);
    }

    private ActivityResult toActivityResult(ActivityDto activityDto) {
        List<MetricValueResult> metricValueResults = activityDto.metricValues().stream()
                .map(this::toMetricValueResult)
                .toList();
        return new ActivityResult(
                activityDto.id(),
                activityDto.title(),
                activityDto.startTime(),
                activityDto.endTime(),
                activityDto.comment(),
                activityDto.tags(),
                metricValueResults
        );
    }

    private MetricValueResult toMetricValueResult(MetricValue metricValue) {
        return new MetricValueResult(metricValue.metricId(), metricValue.value());
    }

    public ActivityResult createActivity(CreateActivityCommand createActivityCommand) {
        List<TagId> tags = createActivityCommand.assignedTags()
                .stream()
                .map(TagId::new)
                .toList();
        List<MetricValue> metricValues = createActivityCommand.metricValueAssignments()
                .stream()
                .map(metricValueAssignment -> new MetricValue(
                        metricValueAssignment.metricId(),
                        metricValueAssignment.metricValue()
                ))
                .toList();

        Activity activity = activityFactory.create(
                createActivityCommand.activityTitle(),
                createActivityCommand.activityStartTime(),
                createActivityCommand.activityEndTime(),
                createActivityCommand.activityComment(),
                tags,
                metricValues
        );
        activityRepository.add(activity);
        activityNotifier.notifyChanged(activity.forChangeNotification());

        return findActivityResult(activity.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find created activity with ID=%s".formatted(activity.id())
                ));
    }

    public SearchResult<ActivityResult> searchActivities(SearchActivitiesQuery searchActivitiesQuery) {

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                actorExtractor.getActor(),
                searchActivitiesQuery.pageSize(),
                searchActivitiesQuery.pageId(),
                searchActivitiesQuery.term(),
                searchActivitiesQuery.timeRangeStart(),
                searchActivitiesQuery.timeRangeEnd(),
                searchActivitiesQuery.excludeFilter(),
                searchActivitiesQuery.tags()
        );

        EntitySearchResult<ActivityDto> searchResult = activitySearchEngine.findActivities(searchCriteria);
        List<ActivityResult> resultForClient = searchResult.results()
                .stream()
                .map(this::toActivityResult)
                .toList();

        return new SearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    // TODO do something with that, more than one aggregate modified
    public ActivityResult switchToNewActivity(SwitchActivityCommand switchActivityCommand) {
        User switcher = actorExtractor.getActor();

        List<TagId> tags = switchActivityCommand.assignedTags()
                .stream()
                .map(TagId::new)
                .toList();
        List<MetricValue> metricValues = switchActivityCommand.metricValueAssignments()
                .stream()
                .map(metricValueAssignment -> new MetricValue(
                        metricValueAssignment.metricId(),
                        metricValueAssignment.metricValue()
                ))
                .toList();

        Activity newActivity = activityFactory.create(
                switchActivityCommand.activityTitle(),
                switchActivityCommand.activityStartTime(),
                switchActivityCommand.activityEndTime(),
                switchActivityCommand.activityComment(),
                tags,
                metricValues
        );
        Instant switchTime = newActivity.isStarted() ? newActivity.startTime() : now();
        newActivity.start(switchTime);

        List<ActivityId> activitiesToFinish = activityDataSource.findOwnUnfinishedStartedBefore(switchTime, switcher);
        finishAllAt(switchTime, activitiesToFinish);

        activityRepository.add(newActivity);
        activityNotifier.notifyChanged(newActivity.forChangeNotification());

        return findActivityResult(newActivity.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find created activity with ID=%s".formatted(newActivity.id())
                ));
    }

    private void finishAllAt(Instant switchTime, List<ActivityId> activitiesToFinish) {
        for (ActivityId activityId : activitiesToFinish) {
            Activity activityToFinish = activityRepository.get(activityId)
                    .orElseThrow(() -> {
                        String message = "Could not find activity to stop with ID=%s".formatted(activityId.id());
                        return new RuntimeException(message);
                    });
            activityToFinish.finish(switchTime);
            activityRepository.save(activityToFinish);
            activityNotifier.notifyChanged(activityToFinish.forChangeNotification());
        }
    }

    public ActivityResult renameActivity(String newTitle, UUID activityId) {
        Activity activity = activityRepository.get(new ActivityId(activityId))
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        activity.rename(newTitle);
        activityRepository.save(activity);
        activityNotifier.notifyChanged(activity.forChangeNotification());

        return findActivityResult(activity.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find updated activity with ID=%s".formatted(activity.id())
                ));
    }

    public ActivityResult startActivity(Instant startTime, UUID activityId) {
        Activity activity = activityRepository.get(new ActivityId(activityId))
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        activity.start(startTime);
        activityRepository.save(activity);
        activityNotifier.notifyChanged(activity.forChangeNotification());

        return findActivityResult(activity.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find updated activity with ID=%s".formatted(activity.id())
                ));
    }

    public ActivityResult finishActivity(Instant endTime, UUID activityId) {
        Activity activity = activityRepository.get(new ActivityId(activityId))
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        activity.finish(endTime);
        activityRepository.save(activity);
        activityNotifier.notifyChanged(activity.forChangeNotification());

        return findActivityResult(activity.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find updated activity with ID=%s".formatted(activity.id())
                ));
    }

    public ActivityResult updateActivityComment(String newComment, UUID activityId) {
        Activity activity = activityRepository.get(new ActivityId(activityId))
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        activity.updateComment(newComment);
        activityRepository.save(activity);
        activityNotifier.notifyChanged(activity.forChangeNotification());

        return findActivityResult(activity.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find updated activity with ID=%s".formatted(activity.id())
                ));
    }

    public ActivityResult addTagToActivity(UUID tagId, UUID activityId) {
        Activity activity = activityRepository.get(new ActivityId(activityId))
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        activity.assignTag(new TagId(tagId));
        activityRepository.save(activity);
        activityNotifier.notifyChanged(activity.forChangeNotification());

        return findActivityResult(activity.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find updated activity with ID=%s".formatted(activity.id())
                ));
    }

    public ActivityResult removeTagFromActivity(UUID tagId, UUID activityId) {
        Activity activity = activityRepository.get(new ActivityId(activityId))
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        activity.removeTag(new TagId(tagId));
        activityRepository.save(activity);
        activityNotifier.notifyChanged(activity.forChangeNotification());

        return findActivityResult(activity.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find updated activity with ID=%s".formatted(activity.id())
                ));
    }

    public ActivityResult setMetricValue(UUID metricId, BigDecimal value, UUID activityId) {
        Activity activity = activityRepository.get(new ActivityId(activityId))
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        activity.setMetricValue(new MetricValue(metricId, value));
        activityRepository.save(activity);
        activityNotifier.notifyChanged(activity.forChangeNotification());

        return findActivityResult(activity.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find updated activity with ID=%s".formatted(activity.id())
                ));
    }

    public ActivityResult unsetMetricValue(UUID metricId, UUID activityId) {
        Activity activity = activityRepository.get(new ActivityId(activityId))
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        activity.unsetMetricValue(new MetricId(metricId));
        activityRepository.save(activity);
        activityNotifier.notifyChanged(activity.forChangeNotification());

        return findActivityResult(activity.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find updated activity with ID=%s".formatted(activity.id())
                ));
    }

    public void deleteActivity(UUID activityId) {
        Activity activity = activityRepository.get(new ActivityId(activityId))
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        activity.delete();
        activityRepository.save(activity);
        activityNotifier.notifyChanged(activity.forChangeNotification());
    }
}
