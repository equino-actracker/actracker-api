package ovh.equino.actracker.domain.tag;

import java.util.UUID;

public record TagChangedNotification(
        TagDto tag
) {

    public UUID id() {
        return tag.id();
    }
}
