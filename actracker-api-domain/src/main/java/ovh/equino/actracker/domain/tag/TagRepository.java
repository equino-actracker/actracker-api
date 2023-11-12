package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TagRepository {

    void add(TagDto tag);

    void update(UUID tagId, TagDto tag);

    // TODO delete when data sources proven
    Optional<TagDto> findById(UUID tagId);

    // TODO delete when data sources proven
    List<TagDto> findByIds(Set<UUID> tagIds, User searcher);

    // TODO delete when data sources proven
    List<TagDto> find(EntitySearchCriteria searchCriteria);
}
