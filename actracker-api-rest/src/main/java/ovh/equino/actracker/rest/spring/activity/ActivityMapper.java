package ovh.equino.actracker.rest.spring.activity;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.rest.spring.PayloadMapper;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toUnmodifiableSet;

class ActivityMapper extends PayloadMapper {

    ActivityDto fromRequest(Activity activityRequest) {

        Set<UUID> tagIds = activityRequest.tags().stream()
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());

        return new ActivityDto(
                timestampToInstant(activityRequest.startTimestamp()),
                timestampToInstant(activityRequest.endTimestamp()),
                activityRequest.comment(),
                tagIds
        );
    }

    Activity toResponse(ActivityDto activity) {

        Set<String> tagIds = activity.tags().stream()
                .map(super::uuidToString)
                .collect(toUnmodifiableSet());

        return new Activity(
                uuidToString(activity.id()),
                instantToTimestamp(activity.startTime()),
                instantToTimestamp(activity.endTime()),
                activity.comment(),
                tagIds
        );
    }

    List<Activity> toResponse(List<ActivityDto> activities) {
        return activities.stream()
                .map(this::toResponse)
                .toList();
    }
}