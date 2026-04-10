package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.EntitySearchCriteria;

import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public record DashboardSearchCriteria(

        EntitySearchCriteria.Common common,
        String term,
        Set<UUID> excludeFilter

) implements EntitySearchCriteria<DashboardDto> {

    private static final String DEFAULT_TERM = "";

    public DashboardSearchCriteria {
        requireNonNull(common);
        term = requireNonNullElse(term, DEFAULT_TERM);
    }
}
