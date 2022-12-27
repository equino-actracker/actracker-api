package ovh.equino.actracker.domain.activity;

import java.time.Instant;
import java.util.UUID;

public record ActivityDto(
        UUID id,
        Instant startTime,
        Instant endTime
) {
}
