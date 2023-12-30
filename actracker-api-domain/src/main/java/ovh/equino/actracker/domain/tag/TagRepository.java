package ovh.equino.actracker.domain.tag;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository {

    Optional<Tag> get(TagId tagId);

    void add(Tag tag);

    // TODO remove, replace with domain events
    void save(Tag tag);
}
