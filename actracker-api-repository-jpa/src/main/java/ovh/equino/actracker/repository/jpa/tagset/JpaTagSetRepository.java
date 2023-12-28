package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tagset.*;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaDAO;
import ovh.equino.actracker.repository.jpa.tag.TagEntity;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

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
