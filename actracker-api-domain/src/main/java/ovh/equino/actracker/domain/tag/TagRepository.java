package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TagRepository {

    void add(TagDto tag);

    void update(UUID tagId, TagDto tag);

    Optional<TagDto> findById(UUID tagId);

    List<TagDto> findByIds(Set<UUID> tagIds, User searcher);

    TagSearchResult find(TagSearchCriteria searchCriteria);
}
