package ovh.equino.actracker.rest.spring.activity;

import org.springframework.web.bind.annotation.*;
import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.application.activity.*;
import ovh.equino.actracker.rest.spring.SearchResponse;
import ovh.equino.actracker.rest.spring.tag.Tag;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNullElse;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/activity")
class ActivityController {

    private final ActivityApplicationService activityApplicationService;
    private final ActivityMapper mapper = new ActivityMapper();

    ActivityController(ActivityApplicationService activityApplicationService) {
        this.activityApplicationService = activityApplicationService;
    }

    @RequestMapping(method = POST)
    @ResponseStatus(OK)
    Activity createActivity(@RequestBody Activity activity) {
        List<MetricValueAssignment> assignedMetricValues =
                requireNonNullElse(activity.metricValues(), new ArrayList<MetricValue>())
                        .stream()
                        .map(metricValue -> new MetricValueAssignment(
                                UUID.fromString(metricValue.metricId()),
                                metricValue.value()
                        ))
                        .toList();

        CreateActivityCommand createActivityCommand = new CreateActivityCommand(
                activity.title(),
                mapper.timestampToInstant(activity.startTimestamp()),
                mapper.timestampToInstant(activity.endTimestamp()),
                activity.comment(),
                mapper.stringsToUuids(activity.tags()),
                assignedMetricValues
        );
        ActivityResult createdActivity = activityApplicationService.createActivity(createActivityCommand);

        return toResponse(createdActivity);
    }

    @RequestMapping(method = GET, path = "/matching")
    @ResponseStatus(OK)
    SearchResponse<Activity> searchActivities(@RequestParam(name = "pageId", required = false) String pageId,
                                              @RequestParam(name = "pageSize", required = false) Integer pageSize,
                                              @RequestParam(name = "term", required = false) String term,
                                              @RequestParam(name = "requiredTags", required = false) String requiredTags,
                                              @RequestParam(name = "excludedActivities", required = false) String excludedActivities,
                                              @RequestParam(name = "rangeStartMillis", required = false) Long rangeStartMillis,
                                              @RequestParam(name = "rangeEndMillis", required = false) Long rangeEndMillis,
                                              @RequestParam(name = "orderBy", required = false) String orderBy) {

        SearchActivitiesQuery searchActivitiesQuery = new SearchActivitiesQuery(
                pageSize,
                pageId,
                term,
                mapper.timestampToInstant(rangeStartMillis),
                mapper.timestampToInstant(rangeEndMillis),
                mapper.parseIds(requiredTags),
                mapper.parseIds(excludedActivities)
        );

        SearchResult<ActivityResult> searchResult = activityApplicationService.searchActivities(searchActivitiesQuery);
        List<Activity> results = searchResult.results().stream()
                .map(this::toResponse)
                .toList();
        return new SearchResponse<>(searchResult.nextPageId(), results);
    }

    @RequestMapping(method = DELETE, path = "/{activityId}")
    @ResponseStatus(OK)
    void deleteActivity(@PathVariable("activityId") String activityId) {
        activityApplicationService.deleteActivity(UUID.fromString(activityId));
    }

    @RequestMapping(method = POST, path = "/switched")
    @ResponseStatus(OK)
    Activity createSwitchedActivity(@RequestBody Activity activity) {

        List<MetricValueAssignment> assignedMetricValues =
                requireNonNullElse(activity.metricValues(), new ArrayList<MetricValue>())
                        .stream()
                        .map(metricValue -> new MetricValueAssignment(
                                UUID.fromString(metricValue.metricId()),
                                metricValue.value()
                        ))
                        .toList();

        SwitchActivityCommand switchActivityCommand = new SwitchActivityCommand(
                activity.title(),
                mapper.timestampToInstant(activity.startTimestamp()),
                mapper.timestampToInstant(activity.endTimestamp()),
                activity.comment(),
                mapper.stringsToUuids(activity.tags()),
                assignedMetricValues
        );
        ActivityResult switchedActivity = activityApplicationService.switchToNewActivity(switchActivityCommand);

        return toResponse(switchedActivity);
    }

    @RequestMapping(method = PUT, path = "/{activityId}/title")
    @ResponseStatus(OK)
    Activity replaceActivityTitle(@PathVariable("activityId") String activityId,
                                  @RequestBody String newTitle) {

        ActivityResult activity = activityApplicationService.renameActivity(newTitle, UUID.fromString(activityId));
        return toResponse(activity);
    }

    @RequestMapping(method = PUT, path = "/{activityId}/startTime")
    @ResponseStatus(OK)
    Activity replaceActivityStartTime(@PathVariable("activityId") String activityId,
                                      @RequestBody Long newStartTimestamp) {

        Instant newStartTime = Instant.ofEpochMilli(newStartTimestamp);
        ActivityResult activity = activityApplicationService.startActivity(newStartTime, UUID.fromString(activityId));
        return toResponse(activity);
    }

    @RequestMapping(method = PUT, path = "/{activityId}/endTime")
    @ResponseStatus(OK)
    Activity replaceActivityEndTime(@PathVariable("activityId") String activityId,
                                    @RequestBody Long newEndTimestamp) {

        Instant newEndTime = Instant.ofEpochMilli(newEndTimestamp);
        ActivityResult activity = activityApplicationService.finishActivity(newEndTime, UUID.fromString(activityId));
        return toResponse(activity);
    }

    @RequestMapping(method = PUT, path = "/{activityId}/comment")
    @ResponseStatus(OK)
    Activity replaceActivityComment(@PathVariable("activityId") String activityId,
                                    @RequestBody String newComment) {

        ActivityResult activity = activityApplicationService.updateActivityComment(newComment, UUID.fromString(activityId));
        return toResponse(activity);
    }

    @RequestMapping(method = POST, path = "/{activityId}/tag")
    @ResponseStatus(OK)
    Activity addTagToActivity(@PathVariable("activityId") String activityId,
                              @RequestBody Tag newTag) {

        UUID tagId = UUID.fromString(newTag.id());
        ActivityResult activity = activityApplicationService.addTagToActivity(tagId, UUID.fromString(activityId));
        return toResponse(activity);
    }

    @RequestMapping(method = DELETE, path = "/{activityId}/tag/{tagId}")
    @ResponseStatus(OK)
    Activity removeTagFromActivity(@PathVariable("activityId") String activityId,
                                   @PathVariable("tagId") String tagId) {

        ActivityResult activity = activityApplicationService.removeTagFromActivity(
                UUID.fromString(tagId),
                UUID.fromString(activityId)
        );
        return toResponse(activity);
    }

    @RequestMapping(method = PUT, path = "/{activityId}/metric/{metricId}/value")
    @ResponseStatus(OK)
    Activity replaceActivityMetricValue(@PathVariable("activityId") String activityId,
                                        @PathVariable("metricId") String metricId,
                                        @RequestBody BigDecimal value) {

        ActivityResult updatedActivity = activityApplicationService.setMetricValue(
                UUID.fromString(metricId),
                value,
                UUID.fromString(activityId)
        );
        return toResponse(updatedActivity);
    }

    @RequestMapping(method = DELETE, path = "/{activityId}/metric/{metricId}/value")
    @ResponseStatus(OK)
    Activity removeActivityMetricValue(@PathVariable("activityId") String activityId,
                                       @PathVariable("metricId") String metricId) {

        ActivityResult updatedActivity = activityApplicationService.unsetMetricValue(
                UUID.fromString(metricId),
                UUID.fromString(activityId)
        );
        return toResponse(updatedActivity);
    }

    private Activity toResponse(ActivityResult activityResult) {
        List<MetricValue> metricValues = activityResult.metricValues().stream()
                .map(this::toResponse)
                .toList();
        return new Activity(
                activityResult.id().toString(),
                activityResult.title(),
                mapper.instantToTimestamp(activityResult.startTime()),
                mapper.instantToTimestamp(activityResult.endTime()),
                activityResult.comment(),
                mapper.uuidsToStrings(activityResult.tags()),
                metricValues
        );
    }

    private MetricValue toResponse(MetricValueResult metricValueResult) {
        return new MetricValue(metricValueResult.metricId().toString(), metricValueResult.value());
    }
}
