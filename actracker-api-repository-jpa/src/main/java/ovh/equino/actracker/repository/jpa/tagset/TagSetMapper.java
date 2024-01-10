package ovh.equino.actracker.repository.jpa.tagset;

import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tagset.TagSet;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetFactory;
import ovh.equino.actracker.domain.tagset.TagSetId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.tag.TagEntity;
import ovh.equino.actracker.jpa.tagset.TagSetEntity;

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
        if (isNull(entity)) {
            return null;
        }
        Set<TagId> tags = requireNonNullElse(entity.getTags(), new ArrayList<TagEntity>())
                .stream()
                .map(tag -> new TagId(tag.getId()))
                .collect(toUnmodifiableSet());

        return tagSetFactory.reconstitute(
                new TagSetId(entity.getId()),
                new User(entity.getCreatorId()),
                entity.getName(),
                tags,
                entity.isDeleted()
        );
    }

    TagSetEntity toEntity(TagSetDto dto) {

        Set<TagEntity> dtoTags = requireNonNullElse(dto.tags(), new HashSet<UUID>()).stream()
                .map(UUID::toString)
                .map(this::toTagEntity)
                .collect(toUnmodifiableSet());

        TagSetEntity entity = new TagSetEntity();
        entity.setId(isNull(dto.id()) ? null : dto.id().toString());
        entity.setCreatorId(isNull(dto.creatorId()) ? null : dto.creatorId().toString());
        entity.setName(dto.name());
        entity.setTags(dtoTags);
        entity.setDeleted(dto.deleted());
        return entity;
    }

    private TagEntity toTagEntity(String tagId) {
        TagEntity tagEntity = new TagEntity();
        tagEntity.setId(tagId);
        return tagEntity;
    }
}
