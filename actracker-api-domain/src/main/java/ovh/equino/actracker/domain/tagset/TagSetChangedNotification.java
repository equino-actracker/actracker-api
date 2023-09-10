package ovh.equino.actracker.domain.tagset;

import java.util.UUID;

public record TagSetChangedNotification(
        TagSetDto tagSet
) {

    public UUID id() {
        return tagSet.id();
    }
}
