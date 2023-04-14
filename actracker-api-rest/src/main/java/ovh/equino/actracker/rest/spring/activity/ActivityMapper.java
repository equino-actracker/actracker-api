package ovh.equino.actracker.rest.spring.activity;

import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.rest.spring.PayloadMapper;
import ovh.equino.actracker.rest.spring.SearchResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;

class ActivityMapper extends PayloadMapper {

    ActivityDto fromRequest(Activity activityRequest) {

        Set<UUID> tagIds = requireNonNullElse(activityRequest.tags(), new HashSet<String>()).stream().map(UUID::fromString).collect(toUnmodifiableSet());

        return new ActivityDto(timestampToInstant(activityRequest.startTimestamp()), timestampToInstant(activityRequest.endTimestamp()), activityRequest.comment(), tagIds);
    }

    Activity toResponse(ActivityDto activity) {

        Set<String> tagIds = activity.tags().stream().map(super::uuidToString).collect(toUnmodifiableSet());

        return new Activity(uuidToString(activity.id()), instantToTimestamp(activity.startTime()), instantToTimestamp(activity.endTime()), activity.comment(), tagIds);
    }

    List<Activity> toResponse(List<ActivityDto> activities) {
        return activities.stream().map(this::toResponse).toList();
    }

    SearchResponse<Activity> toResponse(EntitySearchResult<ActivityDto> activitySearchResult) {
        List<Activity> foundActivities = toResponse(activitySearchResult.results());
        return new SearchResponse<>(activitySearchResult.nextPageId(), foundActivities);
    }
}