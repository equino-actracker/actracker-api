package ovh.equino.actracker.domain.dashboard;

import java.util.Set;
import java.util.UUID;

public record Chart(

        String name,
        GroupBy groupBy,
        Set<UUID> includedTags
) {

    public enum GroupBy {
        TAG,
        DAY
    }
}
