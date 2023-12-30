package ovh.equino.actracker.repository.jpa.tag;

import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.user.User;

import java.util.UUID;

import static java.util.Objects.isNull;

class TagMapper {

    private final TagFactory tagFactory;
    private final MetricMapper metricMapper;
    private final TagShareMapper shareMapper;

    TagMapper(TagFactory tagFactory, MetricFactory metricFactory) {
        this.tagFactory = tagFactory;
        this.metricMapper = new MetricMapper(metricFactory);
        this.shareMapper = new TagShareMapper();
    }

    Tag toDomainObject(TagEntity entity) {
        if (isNull(entity)) {
            return null;
        }
        return tagFactory.reconstitute(
                new TagId(entity.id),
                new User(entity.creatorId),
                entity.name,
                metricMapper.toDomainObjects(entity.metrics),
                shareMapper.toDomainObjects(entity.shares),
                entity.deleted
        );
    }

    TagDto toDto(TagEntity entity) {
        return new TagDto(
                UUID.fromString(entity.id),
                UUID.fromString(entity.creatorId),
                entity.name,
                metricMapper.toDto(entity.metrics),
                shareMapper.toDomainObjects(entity.shares),
                entity.deleted
        );
    }

    TagEntity toEntity(TagDto dto) {
        TagEntity entity = new TagEntity();
        entity.id = isNull(dto.id()) ? null : dto.id().toString();
        entity.creatorId = isNull(dto.creatorId()) ? null : dto.creatorId().toString();
        entity.name = dto.name();
        entity.metrics = metricMapper.toEntities(dto.metrics(), entity);
        entity.shares = shareMapper.toEntities(dto.shares(), entity);
        entity.deleted = dto.deleted();
        return entity;
    }
}
