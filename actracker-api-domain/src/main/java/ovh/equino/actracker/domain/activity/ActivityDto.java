package ovh.equino.actracker.domain.activity;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ActivityDto(

        UUID id,
        UUID creatorId,
        String title,
        Instant startTime,
        Instant endTime,
        String comment,
        Set<UUID> tags,
        List<MetricValue> metricValues,
        boolean deleted

) {

    // Constructor for data provided from input
    public ActivityDto(String title,
                       Instant startTime,
                       Instant endTime,
                       String comment,
                       Set<UUID> tags,
                       List<MetricValue> metricValues) {

        this(null, null, title, startTime, endTime, comment, tags, metricValues, false);
    }
}
