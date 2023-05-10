package ovh.equino.actracker.rest.spring.activity;

import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.rest.spring.PayloadMapper;
import ovh.equino.actracker.rest.spring.SearchResponse;

import java.util.List;

class ActivityMapper extends PayloadMapper {

    ActivityDto fromRequest(Activity activityRequest) {
        return new ActivityDto(
                activityRequest.title(),
                timestampToInstant(activityRequest.startTimestamp()),
                timestampToInstant(activityRequest.endTimestamp()),
                activityRequest.comment(),
                stringsToUuids(activityRequest.tags())
        );
    }

    Activity toResponse(ActivityDto activity) {
        return new Activity(
                uuidToString(activity.id()),
                activity.title(),
                instantToTimestamp(activity.startTime()),
                instantToTimestamp(activity.endTime()),
                activity.comment(),
                uuidsToStrings(activity.tags())
        );
    }

    SearchResponse<Activity> toResponse(EntitySearchResult<ActivityDto> activitySearchResult) {
        List<Activity> foundActivities = toResponse(activitySearchResult.results());
        return new SearchResponse<>(activitySearchResult.nextPageId(), foundActivities);
    }

    private List<Activity> toResponse(List<ActivityDto> activities) {
        return activities.stream()
                .map(this::toResponse)
                .toList();
    }
}