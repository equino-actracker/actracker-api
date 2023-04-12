package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;
import ovh.equino.actracker.domain.tag.TagSearchResult;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaRepository;

import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

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
                                hasId(tagId, criteriaBuilder, rootEntity),
                                isNotDeleted(criteriaBuilder, rootEntity)
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
                                isAccessibleFor(searcher, criteriaBuilder, rootEntity),
                                isNotDeleted(criteriaBuilder, rootEntity)
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
        Set<UUID> excludedIds = searchCriteria.excludeFilter();
        User searcher = searchCriteria.searcher();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TagEntity> criteriaQuery = criteriaBuilder.createQuery(TagEntity.class);
        Root<TagEntity> rootEntity = criteriaQuery.from(TagEntity.class);

        CriteriaQuery<TagEntity> query = criteriaQuery
                .select(rootEntity)
                .where(
                        criteriaBuilder.and(
                                isAccessibleFor(searcher, criteriaBuilder, rootEntity),
                                isNotDeleted(criteriaBuilder, rootEntity),
                                isInPage(pageId, criteriaBuilder, rootEntity),
                                isNotExcluded(excludedIds, criteriaBuilder, rootEntity)
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

    private String getNextPageId(List<TagDto> foundTags, String currentPageId) {
        if (isEmpty(foundTags)) {
            return currentPageId;
        }
        TagDto lastTag = new LinkedList<>(foundTags).getLast();
        return lastTag.id().toString();
    }

    private Predicate hasId(UUID tagId, CriteriaBuilder criteriaBuilder, Root<TagEntity> rootEntity) {
        return criteriaBuilder.equal(rootEntity.get("id"), tagId.toString());
    }

    private Predicate isAccessibleFor(User searcher, CriteriaBuilder criteriaBuilder, Root<TagEntity> rootEntity) {
        return criteriaBuilder.equal(
                rootEntity.get("creatorId"),
                searcher.id().toString()
        );
    }

    private Predicate isNotDeleted(CriteriaBuilder criteriaBuilder, Root<TagEntity> rootEntity) {
        return criteriaBuilder.isFalse(rootEntity.get("deleted"));
    }

    private Predicate isNotExcluded(Set<UUID> excludedIds, CriteriaBuilder criteriaBuilder, Root<TagEntity> rootEntity) {
        if (isEmpty(excludedIds)) {
            return criteriaBuilder.and();   // always true
        }
        Path<Object> id = rootEntity.get("id");
        CriteriaBuilder.In<Object> idIn = criteriaBuilder.in(id);
        excludedIds.stream()
                .map(UUID::toString)
                .forEach(idIn::value);
        return criteriaBuilder.not(idIn);
    }

    private Predicate isInPage(String pageId, CriteriaBuilder criteriaBuilder, Root<TagEntity> rootEntity) {
        if (isBlank(pageId)) {
            return criteriaBuilder.and();   // always true
        }
        return criteriaBuilder.greaterThan(
                rootEntity.get("id"),
                pageId
        );
    }
}
