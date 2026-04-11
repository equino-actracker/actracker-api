package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySortCriteria;

import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public record TagSetSearchCriteria(

        EntitySearchCriteria.Common common,
        String term,
        Set<UUID> excludeFilter

) implements EntitySearchCriteria<TagSetDto> {

    private static final String DEFAULT_TERM = "";

    public TagSetSearchCriteria {
        requireNonNull(common);
        term = requireNonNullElse(term, DEFAULT_TERM);
    }

    public enum SortableField implements EntitySortCriteria.Field {

    }
}
