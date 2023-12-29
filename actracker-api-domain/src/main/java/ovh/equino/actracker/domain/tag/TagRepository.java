package ovh.equino.actracker.domain.tag;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository {

    // TODO remove
    void add(TagDto tag);

    // TODO remove
    void update(UUID tagId, TagDto tag);

    // TODO remove
    Optional<TagDto> findById(UUID tagId);

    Optional<Tag> get(TagId tagId);

    void add(Tag tag);

    // TODO remove, replace with domain events
    void save(Tag tag);
}
