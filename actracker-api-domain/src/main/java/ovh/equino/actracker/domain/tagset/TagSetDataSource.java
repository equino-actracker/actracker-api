package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Optional;

public interface TagSetDataSource {

    Optional<TagSetDto> find(TagSetId tagSetId, User searcher);

    List<TagSetDto> find(EntitySearchCriteria searchCriteria);
}
