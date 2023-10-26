package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

class JpaTagRepository extends JpaDAO implements TagRepository {

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

    @Override
    public List<TagDto> findByIds(Set<UUID> tagIds, User searcher) {

        TagQueryBuilder queryBuilder = new TagQueryBuilder(entityManager);

        CriteriaQuery<TagEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.isAccessibleFor(searcher),
                                queryBuilder.hasId(tagIds)
                        )
                );

        TypedQuery<TagEntity> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<TagDto> find(EntitySearchCriteria searchCriteria) {

        TagQueryBuilder queryBuilder = new TagQueryBuilder(entityManager);

        CriteriaQuery<TagEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.isAccessibleFor(searchCriteria.searcher()),
                                queryBuilder.isNotDeleted(),
                                queryBuilder.isInPage(searchCriteria.pageId()),
                                queryBuilder.isNotExcluded(searchCriteria.excludeFilter()),
                                queryBuilder.matchesTerm(searchCriteria.term())
                        )
                )
                .orderBy(queryBuilder.ascending("id"));

        TypedQuery<TagEntity> typedQuery = entityManager
                .createQuery(query)
                .setMaxResults(searchCriteria.pageSize());

        return typedQuery.getResultList().stream()
                .map(mapper::toDto)
                .toList();
    }
}
