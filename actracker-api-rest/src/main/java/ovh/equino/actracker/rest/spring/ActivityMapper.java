package ovh.equino.actracker.rest.spring;

import ovh.equino.actracker.domain.activity.ActivityDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

class ActivityMapper {

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

    private Instant timestampToInstant(Long timestamp) {
        if (isNull(timestamp)) {
            return null;
        }
        return Instant.ofEpochMilli(timestamp);
    }

    private Long instantToTimestamp(Instant instant) {
        if (isNull(instant)) {
            return null;
        }
        return instant.toEpochMilli();
    }

    private String uuidToString(UUID uuid) {
        if (isNull(uuid)) {
            return null;
        }
        return uuid.toString();
    }
}
