package ovh.equino.actracker.domain.tag;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository {

    void add(TagDto tag);

    void update(UUID tagId, TagDto tag);

    // TODO delete when data sources proven
    Optional<TagDto> findById(UUID tagId);
}
