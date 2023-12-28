package ovh.equino.actracker.domain.tagset;

import java.util.Optional;

public interface TagSetRepository {

    Optional<TagSet> get(TagSetId tagSetId);

    void add(TagSet tagSet);

    // TODO remove, replace with domain events
    void save(TagSet tagSet);
}
