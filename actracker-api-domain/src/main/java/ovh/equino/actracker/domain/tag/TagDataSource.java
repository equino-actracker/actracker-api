package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TagDataSource {

    Optional<TagDto> find(TagId tagId, User searcher);

    List<TagDto> find(EntitySearchCriteria searchCriteria);

    List<TagDto> find(Set<TagId> tagIds, User searcher);
}
