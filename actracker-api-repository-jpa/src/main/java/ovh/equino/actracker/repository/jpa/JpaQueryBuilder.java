package ovh.equino.actracker.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.exception.InvalidSortFieldException;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class JpaQueryBuilder<ENTITY> {

    protected final CriteriaBuilder criteriaBuilder;
    protected final CriteriaQuery<ENTITY> criteriaQuery;
    protected final Root<ENTITY> rootEntity;

    private final Map<EntitySortCriteria.Field, String> sortFieldToEntityField;


    protected JpaQueryBuilder(EntityManager entityManager,
                              Class<ENTITY> entityType,
                              Map<EntitySortCriteria.Field, String> sortFieldToEntityField) {

        criteriaBuilder = entityManager.getCriteriaBuilder();
        criteriaQuery = criteriaBuilder.createQuery(entityType);
        rootEntity = criteriaQuery.from(entityType);
        this.sortFieldToEntityField = sortFieldToEntityField;
    }

    public CriteriaQuery<ENTITY> select() {
        return criteriaQuery.select(rootEntity).distinct(true);
    }

    public Predicate hasId(UUID id) {
        return criteriaBuilder.equal(rootEntity.get("id"), id.toString());
    }

    public Predicate hasId(Set<UUID> ids) {
        if (isEmpty(ids)) {
            return noneMatch();
        }
        Path<Object> id = rootEntity.get("id");
        CriteriaBuilder.In<Object> idIn = criteriaBuilder.in(id);
        ids.stream()
                .map(UUID::toString)
                .forEach(idIn::value);
        return idIn;
    }

    public Predicate isAccessibleFor(User searcher) {
        return criteriaBuilder.equal(
                rootEntity.get("creatorId"),
                searcher.id().toString()
        );
    }

    public Predicate isNotDeleted() {
        return criteriaBuilder.isFalse(rootEntity.get("deleted"));
    }

    public Predicate isNotExcluded(Set<UUID> excludedIds) {
        if (isEmpty(excludedIds)) {
            return allMatch();
        }
        Path<Object> id = rootEntity.get("id");
        CriteriaBuilder.In<Object> idIn = criteriaBuilder.in(id);
        excludedIds.stream()
                .map(UUID::toString)
                .forEach(idIn::value);
        return criteriaBuilder.not(idIn);
    }

    public Predicate isInPage(String pageId) {
        if (isBlank(pageId)) {
            return allMatch();
        }
        return criteriaBuilder.greaterThanOrEqualTo(
                rootEntity.get("id"),
                pageId
        );
    }

    public Predicate allMatch() {
        return and();
    }

    public Predicate noneMatch() {
        return or();
    }

    protected Predicate matchesTerm(String term, String searchFieldName) {
        if (isBlank(term)) {
            return allMatch();
        }
        return criteriaBuilder.like(
                rootEntity.get(searchFieldName),
                term + "%"
        );
    }

    public Predicate and(Predicate... predicates) {
        return criteriaBuilder.and(predicates);
    }

    public Predicate or(Predicate... predicates) {
        return criteriaBuilder.or(predicates);
    }

    public Order ascending(String fieldName) {
        return criteriaBuilder.asc(rootEntity.get(fieldName));
    }

    public Order descending(String fieldName) {
        return criteriaBuilder.desc(rootEntity.get(fieldName));
    }

    public List<Order> sortingSequence(EntitySortCriteria sortCriteria) {
        return sortCriteria.levels().stream()
                .map(this::sortingSequence)
                .toList();
    }

    private Order sortingSequence(EntitySortCriteria.Level sortLevel) {
        String entityField = sortFieldToEntityField.get(sortLevel.field());

        if (entityField == null) {
            throw new InvalidSortFieldException(sortLevel.field().toString());
        }

        return switch (sortLevel.order()) {
            case ASC -> ascending(entityField);
            case DESC -> descending(entityField);
        };
    }
}
