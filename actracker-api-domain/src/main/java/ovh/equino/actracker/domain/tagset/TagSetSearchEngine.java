package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.EntitySearchResult;

public interface TagSetSearchEngine {

    EntitySearchResult<TagSetDto> findTagSets(TagSetSearchCriteria searchCriteria);
}
