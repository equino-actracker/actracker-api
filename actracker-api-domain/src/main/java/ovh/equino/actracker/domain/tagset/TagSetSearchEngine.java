package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;

public interface TagSetSearchEngine {

    EntitySearchResult<TagSetDto> findTagSets(EntitySearchCriteria searchCriteria);
}
