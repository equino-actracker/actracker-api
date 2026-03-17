package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagDataSource {

    Optional<TagDto> find(TagId tagId, User searcher);

    List<TagDto> find(TagSearchCriteria searchCriteria);

    List<TagDto> find(Set<TagId> tagIds, User searcher);
}
