package ovh.equino.actracker.domain.tagset;

import java.util.Optional;
import java.util.UUID;

public interface TagSetRepository {

    void add(TagSetDto tagSet);

    void update(UUID tagSetId, TagSetDto tagSet);

    // TODO delete when data sources proven
    Optional<TagSetDto> findById(UUID tagSetId);
}
