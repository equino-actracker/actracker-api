package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.EntitySearchCriteria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagSetRepository {

    void add(TagSetDto tagSet);

    void update(UUID tagSetId, TagSetDto tagSet);

    // TODO delete when data sources proven
    Optional<TagSetDto> findById(UUID tagSetId);

    // TODO delete when data sources proven
    List<TagSetDto> find(EntitySearchCriteria searchCriteria);
}
