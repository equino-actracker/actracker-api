package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.CommonSearchCriteria;

import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public record TagSetSearchCriteria(

        CommonSearchCriteria common,
        String term,
        Set<UUID> excludeFilter

) {

    private static final String DEFAULT_TERM = "";

    public TagSetSearchCriteria {
        requireNonNull(common);
        term = requireNonNullElse(term, DEFAULT_TERM);
    }
}
