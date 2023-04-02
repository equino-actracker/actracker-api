package ovh.equino.actracker.rest.spring.activity;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.rest.spring.PayloadMapper;

import java.util.List;

class ActivityMapper extends PayloadMapper {

    ActivityDto fromRequest(Activity activityRequest) {
        return new ActivityDto(
                timestampToInstant(activityRequest.startTimestamp()),
                timestampToInstant(activityRequest.endTimestamp()),
                activityRequest.comment()
        );
    }

    Activity toResponse(ActivityDto activity) {
        return new Activity(
                uuidToString(activity.id()),
                instantToTimestamp(activity.startTime()),
                instantToTimestamp(activity.endTime()),
                activity.comment()
        );
    }

    List<Activity> toResponse(List<ActivityDto> activities) {
        return activities.stream()
                .map(this::toResponse)
                .toList();
    }
}