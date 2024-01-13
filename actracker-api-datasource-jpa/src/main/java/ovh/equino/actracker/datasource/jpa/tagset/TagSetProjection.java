package ovh.equino.actracker.datasource.jpa.tagset;

import ovh.equino.actracker.domain.tagset.TagSetDto;

import java.util.Set;
import java.util.UUID;

record TagSetProjection(String id,
                        String creatorId,
                        String name,
                        Boolean deleted
) {

    TagSetDto toTagSet(Set<UUID> tagIds) {
        return new TagSetDto(
                UUID.fromString(id()),
                UUID.fromString(creatorId()),
                name(),
                tagIds,
                deleted()
        );
    }
}
