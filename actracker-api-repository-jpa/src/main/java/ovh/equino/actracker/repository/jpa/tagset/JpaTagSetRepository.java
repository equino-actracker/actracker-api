package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.tagset.*;
import ovh.equino.actracker.jpa.tagset.TagSetEntity;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.Optional;

import static java.util.Objects.nonNull;

class JpaTagSetRepository extends JpaDAO implements TagSetRepository {

    private final TagSetMapper tagSetMapper;

    JpaTagSetRepository(EntityManager entityManager, TagSetFactory tagSetFactory) {
        super(entityManager);
        this.tagSetMapper = new TagSetMapper(tagSetFactory);
    }

    @Override
    public Optional<TagSet> get(TagSetId tagSetId) {
        TagSetEntity entity = entityManager.find(TagSetEntity.class, tagSetId.id().toString());
        TagSet tagSet = tagSetMapper.toDomainObject(entity);
        if (nonNull(entity)) {
            entityManager.detach(entity);
        }
        return Optional.ofNullable(tagSet);
    }

    @Override
    public void add(TagSet tagSet) {
        TagSetDto dto = tagSet.forStorage();
        TagSetEntity entity = tagSetMapper.toEntity(dto);
        entityManager.persist(entity);
    }

    @Override
    public void save(TagSet tagSet) {
        TagSetDto dto = tagSet.forStorage();
        TagSetEntity entity = tagSetMapper.toEntity(dto);
        entityManager.merge(entity);
    }
}
