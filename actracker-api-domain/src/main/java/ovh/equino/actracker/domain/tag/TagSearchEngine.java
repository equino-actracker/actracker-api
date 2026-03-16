package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.EntitySearchResult;

public interface TagSearchEngine {

    EntitySearchResult<TagDto> findTags(TagSearchCriteria searchCriteria);
}
