package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.CommonSearchCriteria;

import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public record TagSearchCriteria(

        CommonSearchCriteria common,
        String term,
        Set<UUID> excludeFilter

) {

    private static final String DEFAULT_TERM = "";

    public TagSearchCriteria {
        requireNonNull(common);
        term = requireNonNullElse(term, DEFAULT_TERM);
    }
}
