package ovh.equino.actracker.domain.tagset;

import java.util.Optional;
import java.util.UUID;

public interface TagSetRepository {

    // TODO remove
    void add(TagSetDto tagSet);

    // TODO remove
    void update(UUID tagSetId, TagSetDto tagSet);

    // TODO remove
    Optional<TagSetDto> findById(UUID tagSetId);

//    Optional<TagSet> get(TagSetId tagSetId);

//    void add(TagSet tagSet);

    // TODO remove, replace with domain events
//    void save(TagSet tagSet);
}
