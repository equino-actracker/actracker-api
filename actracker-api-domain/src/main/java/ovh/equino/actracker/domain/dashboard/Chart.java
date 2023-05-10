package ovh.equino.actracker.domain.dashboard;

import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNullElse;

public record Chart(

        String name,
        GroupBy groupBy,
        Set<UUID> includedTags
) {

    public Chart {
        includedTags = requireNonNullElse(includedTags, emptySet());
    }

    public boolean includesAllTags() {
        return includedTags.isEmpty();
    }

    public enum GroupBy {
        TAG,
        DAY
    }
}
