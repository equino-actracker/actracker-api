package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tagset.*;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaDAO;
import ovh.equino.actracker.repository.jpa.tag.TagEntity;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;

class JpaTagSetRepository extends JpaDAO implements TagSetRepository {

    private final TagSetFactory tagSetFactory;

    JpaTagSetRepository(EntityManager entityManager, TagSetFactory tagSetFactory) {
        super(entityManager);
        this.tagSetFactory = tagSetFactory;
    }

    private final TagSetMapper mapper = new TagSetMapper();

    @Override
    public void add(TagSetDto tagSet) {
        TagSetEntity tagSetEntity = mapper.toEntity(tagSet);
        entityManager.persist(tagSetEntity);
    }

    @Override
    public void update(UUID tagSetId, TagSetDto tagSet) {
        TagSetEntity tagSetEntity = mapper.toEntity(tagSet);
        tagSetEntity.id = tagSetId.toString();
        entityManager.merge(tagSetEntity);
    }

    @Override
    public Optional<TagSetDto> findById(UUID tagSetId) {
        TagSetQueryBuilder queryBuilder = new TagSetQueryBuilder(entityManager);

        CriteriaQuery<TagSetEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.hasId(tagSetId),
                                queryBuilder.isNotDeleted()
                        )
                );

        TypedQuery<TagSetEntity> typedQuery = entityManager.createQuery(query);

        return typedQuery.getResultList().stream()
                .findFirst()
                .map(mapper::toDto);
    }

    @Override
    public Optional<TagSet> get(TagSetId tagSetId) {

        TagSetEntity entity = entityManager.find(TagSetEntity.class, tagSetId.id().toString());
        if (isNull(entity)) {
            return Optional.empty();
        }

        Set<TagId> tags = requireNonNullElse(entity.tags, new ArrayList<TagEntity>())
                .stream()
                .map(tag -> new TagId(tag.id))
                .collect(toUnmodifiableSet());

        TagSet tagSet = tagSetFactory.reconstitute(
                new TagSetId(entity.id),
                new User(entity.creatorId),
                entity.name,
                tags,
                entity.deleted
        );
        return Optional.of(tagSet);
    }

    @Override
    public void add(TagSet tagSet) {
        TagSetDto dto = tagSet.forStorage();
        TagSetEntity entity = mapper.toEntity(dto);
        entityManager.persist(entity);
    }

    @Override
    public void save(TagSet tagSet) {
        TagSetDto dto = tagSet.forStorage();
        TagSetEntity entity = mapper.toEntity(dto);
        entityManager.merge(entity);
    }
}
