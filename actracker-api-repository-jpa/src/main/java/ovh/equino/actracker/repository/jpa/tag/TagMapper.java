package ovh.equino.actracker.repository.jpa.tag;

import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.tag.TagEntity;

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
                new TagId(entity.getId()),
                new User(entity.getCreatorId()),
                entity.getName(),
                metricMapper.toDomainObjects(entity.getMetrics()),
                shareMapper.toDomainObjects(entity.getShares()),
                entity.isDeleted()
        );
    }

    TagEntity toEntity(TagDto dto) {
        TagEntity entity = new TagEntity();
        entity.setId(isNull(dto.id()) ? null : dto.id().toString());
        entity.setCreatorId(isNull(dto.creatorId()) ? null : dto.creatorId().toString());
        entity.setName(dto.name());
        entity.setMetrics(metricMapper.toEntities(dto.metrics(), entity));
        entity.setShares(shareMapper.toEntities(dto.shares(), entity));
        entity.setDeleted(dto.deleted());
        return entity;
    }
}
