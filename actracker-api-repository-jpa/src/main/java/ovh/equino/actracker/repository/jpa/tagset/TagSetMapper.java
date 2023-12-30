package ovh.equino.actracker.repository.jpa.tagset;

import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tagset.TagSet;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetFactory;
import ovh.equino.actracker.domain.tagset.TagSetId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.tag.TagEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;

class TagSetMapper {

    private final TagSetFactory tagSetFactory;

    TagSetMapper(TagSetFactory tagSetFactory) {
        this.tagSetFactory = tagSetFactory;
    }

    TagSet toDomainObject(TagSetEntity entity) {
        if(isNull(entity)) {
            return null;
        }
        Set<TagId> tags = requireNonNullElse(entity.tags, new ArrayList<TagEntity>())
                .stream()
                .map(tag -> new TagId(tag.id))
                .collect(toUnmodifiableSet());

        return tagSetFactory.reconstitute(
                new TagSetId(entity.id),
                new User(entity.creatorId),
                entity.name,
                tags,
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
