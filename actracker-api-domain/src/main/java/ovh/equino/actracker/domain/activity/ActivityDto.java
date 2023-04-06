package ovh.equino.actracker.domain.activity;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ActivityDto(

        UUID id,
        UUID creatorId,
        Instant startTime,
        Instant endTime,
        String comment,
        Set<UUID> tags,
        boolean deleted

) {

    // Constructor for data provided from input
    public ActivityDto(Instant startTime, Instant endTime, String comment, Set<UUID> tags) {
        this(null, null, startTime, endTime, comment, tags, false);
    }
}
