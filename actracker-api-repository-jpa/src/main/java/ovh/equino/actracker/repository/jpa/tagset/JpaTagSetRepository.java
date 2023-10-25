package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetRepository;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

class JpaTagSetRepository extends JpaDAO implements TagSetRepository {

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
    public List<TagSetDto> find(EntitySearchCriteria searchCriteria) {
        TagSetQueryBuilder queryBuilder = new TagSetQueryBuilder(entityManager);

        CriteriaQuery<TagSetEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.isAccessibleFor(searchCriteria.searcher()),
                                queryBuilder.isNotDeleted(),
                                queryBuilder.isInPage(searchCriteria.pageId()),
                                queryBuilder.isNotExcluded(searchCriteria.excludeFilter())
                        )
                )
                .orderBy(queryBuilder.ascending("id"));

        TypedQuery<TagSetEntity> typedQuery = entityManager
                .createQuery(query)
                .setMaxResults(searchCriteria.pageSize());

        return typedQuery.getResultList().stream()
                .map(mapper::toDto)
                .toList();
    }
}
