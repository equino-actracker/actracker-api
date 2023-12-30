package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.Optional;
import java.util.UUID;

class JpaTagRepository extends JpaDAO implements TagRepository {

    private final TagMapper tagMapper;

    JpaTagRepository(EntityManager entityManager, TagFactory tagFactory, MetricFactory metricFactory) {
        super(entityManager);
        this.tagMapper = new TagMapper(tagFactory, metricFactory);
    }

    @Override
    public Optional<Tag> get(TagId tagId) {
        TagEntity entity = entityManager.find(TagEntity.class, tagId.id().toString());
        Tag tag = tagMapper.toDomainObject(entity);
        return Optional.ofNullable(tag);
    }

    @Override
    public void add(Tag tag) {
        TagDto dto = tag.forStorage();
        TagEntity entity = tagMapper.toEntity(dto);
        entityManager.persist(entity);
    }

    @Override
    public void save(Tag tag) {
        TagDto dto = tag.forStorage();
        TagEntity entity = tagMapper.toEntity(dto);
        entityManager.merge(entity);
    }
}
