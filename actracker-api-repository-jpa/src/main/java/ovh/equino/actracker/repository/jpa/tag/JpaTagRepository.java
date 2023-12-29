package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagFactory;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.Optional;
import java.util.UUID;

class JpaTagRepository extends JpaDAO implements TagRepository {

    private final TagFactory tagFactory;

    JpaTagRepository(EntityManager entityManager, TagFactory tagFactory) {
        super(entityManager);
        this.tagFactory = tagFactory;
    }

    private final TagMapper mapper = new TagMapper();

    @Override
    public void add(TagDto tag) {
        TagEntity tagEntity = mapper.toEntity(tag);
        entityManager.persist(tagEntity);
    }

    @Override
    public void update(UUID tagId, TagDto tag) {
        TagEntity tagEntity = mapper.toEntity(tag);
        entityManager.merge(tagEntity);
    }

    @Override
    public Optional<TagDto> findById(UUID tagId) {

        TagQueryBuilder queryBuilder = new TagQueryBuilder(entityManager);

        // If Hibernate were used instead of JPA API, filters could be used instead for soft delete:
        // https://www.baeldung.com/spring-jpa-soft-delete
        CriteriaQuery<TagEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.hasId(tagId),
                                queryBuilder.isNotDeleted()
                        )
                );

        TypedQuery<TagEntity> typedQuery = entityManager.createQuery(query);

        // If Hibernate were used instead of JPA API, result transformers could do mapping rather than custom mapper:
        // https://thorben-janssen.com/object-mapper-dto/
        return typedQuery.getResultList().stream()
                .findFirst()
                .map(mapper::toDto);
    }
}
