package ovh.equino.actracker.repository.jpa.tagset;

import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.repository.jpa.tag.TagEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;

class TagSetMapper {

    TagSetDto toDto(TagSetEntity entity) {

        Set<UUID> entityTags = requireNonNullElse(entity.tags, new HashSet<TagEntity>()).stream()
                .map(tag -> tag.id)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());

        return new TagSetDto(
                UUID.fromString(entity.id),
                UUID.fromString(entity.creatorId),
                entity.name,
                entityTags,
                entity.deleted
        );
    }

    TagSetEntity toEntity(TagSetDto dto) {

        Set<TagEntity> dtoTags = requireNonNullElse(dto.tags(), new HashSet<UUID>()).stream()
                .map(UUID::toString)
                .map(this::toTagEntity)
                .collect(toUnmodifiableSet());

        TagSetEntity entity = new TagSetEntity();
        entity.id = isNull(dto.id()) ? null : dto.id().toString();
        entity.creatorId = isNull(dto.creatorId()) ? null : dto.creatorId().toString();
        entity.name = dto.name();
        entity.tags = dtoTags;
        entity.deleted = dto.deleted();
        return entity;
    }

    private TagEntity toTagEntity(String tagId) {
        TagEntity tagEntity = new TagEntity();
        tagEntity.id = tagId;
        return tagEntity;
    }
}
