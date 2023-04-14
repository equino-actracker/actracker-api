package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.EntitySearchCriteria;

public interface TagSearchEngine {

    EntitySearchResult<TagDto> findTags(EntitySearchCriteria searchCriteria);
}
