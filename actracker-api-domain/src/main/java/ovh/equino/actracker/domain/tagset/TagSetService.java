package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.user.User;

import java.util.UUID;

public interface TagSetService {

    TagSetDto createTagSet(TagSetDto newTagSetData, User creator);

    TagSetDto updateTagSet(UUID tagSetId, TagSetDto updatedTagSetData, User updater);

    EntitySearchResult<TagSetDto> searchTagSets(EntitySearchCriteria searchCriteria);

    void deleteTagSet(UUID tagSetId, User remover);
}
