package ovh.equino.actracker.domain.activity;

import java.time.Instant;
import java.util.UUID;

public record ActivityDto(

        UUID id,
        UUID creatorId,
        Instant startTime,
        Instant endTime,
        boolean deleted

) {

    // Constructor for data provided from input
    public ActivityDto(Instant startTime, Instant endTime) {
        this(null, null, startTime, endTime, false);
    }
}
