package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;
import ovh.equino.actracker.domain.tag.TagSearchResult;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

class JpaTagRepository extends JpaRepository implements TagRepository {

    private final TagMapper mapper = new TagMapper();

    @Override
    public void add(TagDto tag) {
        TagEntity tagEntity = mapper.toEntity(tag);
        entityManager.persist(tagEntity);
    }

    @Override
    public void update(UUID tagId, TagDto tag) {
        TagEntity tagEntity = mapper.toEntity(tag);
        tagEntity.id = tagId.toString();
        entityManager.merge(tagEntity);
    }

    @Override
    public Optional<TagDto> findById(UUID tagId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TagEntity> criteriaQuery = criteriaBuilder.createQuery(TagEntity.class);
        Root<TagEntity> rootEntity = criteriaQuery.from(TagEntity.class);

        // If Hibernate were used instead of JPA API, filters could be used instead for soft delete:
        // https://www.baeldung.com/spring-jpa-soft-delete
        CriteriaQuery<TagEntity> query = criteriaQuery
                .select(rootEntity)
                .where(
                        criteriaBuilder.and(
                                criteriaBuilder.equal(rootEntity.get("id"), tagId.toString()),
                                criteriaBuilder.isFalse(rootEntity.get("deleted"))
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
    public List<TagDto> findAll(User searcher) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TagEntity> criteriaQuery = criteriaBuilder.createQuery(TagEntity.class);
        Root<TagEntity> rootEntity = criteriaQuery.from(TagEntity.class);

        CriteriaQuery<TagEntity> query = criteriaQuery
                .select(rootEntity)
                .where(
                        criteriaBuilder.and(
                                criteriaBuilder.equal(
                                        rootEntity.get("creatorId"),
                                        searcher.id().toString()
                                ),
                                criteriaBuilder.isFalse(rootEntity.get("deleted"))
                        )
                );

        TypedQuery<TagEntity> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public TagSearchResult find(TagSearchCriteria searchCriteria) {
        String pageId = searchCriteria.pageId();
        Integer pageSize = searchCriteria.pageSize();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TagEntity> criteriaQuery = criteriaBuilder.createQuery(TagEntity.class);
        Root<TagEntity> rootEntity = criteriaQuery.from(TagEntity.class);

        CriteriaQuery<TagEntity> query = criteriaQuery
                .select(rootEntity)
                .where(
                        criteriaBuilder.and(
                                criteriaBuilder.equal(
                                        rootEntity.get("creatorId"),
                                        searchCriteria.searcher().id().toString()
                                ),
                                criteriaBuilder.greaterThan(
                                        rootEntity.get("id"),
                                        pageId
                                ),
                                criteriaBuilder.isFalse(rootEntity.get("deleted"))
                        )
                )
                .orderBy(criteriaBuilder.asc(rootEntity.get("id")));

        TypedQuery<TagEntity> typedQuery = entityManager
                .createQuery(query)
                .setMaxResults(pageSize);

        List<TagDto> foundTags = typedQuery.getResultList().stream()
                .map(mapper::toDto)
                .toList();

        String nextPageId = getNextPageId(foundTags, pageId);

        return new TagSearchResult(nextPageId, foundTags);
    }

    private static String getNextPageId(List<TagDto> foundTags, String currentPageId) {
        if (isEmpty(foundTags)) {
            return currentPageId;
        }
        TagDto lastTag = new LinkedList<>(foundTags).getLast();
        return lastTag.id().toString();
    }
}
