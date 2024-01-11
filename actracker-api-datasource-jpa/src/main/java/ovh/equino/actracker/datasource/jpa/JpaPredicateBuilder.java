package ovh.equino.actracker.datasource.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import ovh.equino.actracker.domain.user.User;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toUnmodifiableSet;

public abstract class JpaPredicateBuilder<E> {

    private final CriteriaBuilder criteriaBuilder;
    private final Root<E> root;

    protected JpaPredicateBuilder(CriteriaBuilder criteriaBuilder, Root<E> root) {
        this.criteriaBuilder = criteriaBuilder;
        this.root = root;
    }

    public JpaPredicate hasId(UUID id) {
        return () -> criteriaBuilder.equal(root.get("id"), id.toString());
    }

    public JpaPredicate hasIdIn(Collection<UUID> ids) {
        Set<String> idsAsStrings = ids
                .stream()
                .map(UUID::toString)
                .collect(toUnmodifiableSet());
        return in(idsAsStrings, root.get("id"));
    }

    protected <T> JpaPredicate in(Collection<T> values, Path<T> field) {
        if (CollectionUtils.isEmpty(values)) {
            return noneMatch();
        }
        CriteriaBuilder.In<Object> in = criteriaBuilder.in(field);
        values.stream()
                .collect(toUnmodifiableSet())
                .forEach(in::value);
        return () -> in;
    }

    public JpaPredicate isAccessibleFor(User searcher) {
        return isOwner(searcher);
    }

    public JpaPredicate isOwner(User searcher) {
        return () -> criteriaBuilder.equal(
                root.get("creatorId"),
                searcher.id().toString()
        );
    }

    public JpaPredicate isNotDeleted() {
        return () -> criteriaBuilder.isFalse(root.get("deleted"));
    }

    public JpaPredicate isNotExcluded(Collection<UUID> excludedIds) {
        if (CollectionUtils.isEmpty(excludedIds)) {
            return allMatch();
        }
        Path<Object> id = root.get("id");
        CriteriaBuilder.In<Object> idIn = criteriaBuilder.in(id);
        excludedIds.stream()
                .map(UUID::toString)
                .collect(toUnmodifiableSet())
                .forEach(idIn::value);
        return () -> criteriaBuilder.not(idIn);
    }

    public JpaPredicate isInPage(String pageId) {
        if (StringUtils.isBlank(pageId)) {
            return allMatch();
        }
        return () -> criteriaBuilder.greaterThanOrEqualTo(
                root.get("id"),
                pageId
        );
    }

    protected JpaPredicate matchesTerm(String term, String searchFieldName) {
        if (StringUtils.isBlank(term)) {
            return allMatch();
        }
        return () -> criteriaBuilder.like(
                root.get(searchFieldName),
                term + "%"
        );
    }

    public JpaPredicate not(JpaPredicate predicate) {
        return () -> criteriaBuilder.not(predicate.toRawPredicate());
    }

    public JpaPredicate and(JpaPredicate... predicates) {
        Predicate[] mappedPredicates = stream(predicates)
                .map(JpaPredicate::toRawPredicate)
                .toArray(Predicate[]::new);
        return () -> criteriaBuilder.and(mappedPredicates);
    }

    public JpaPredicate or(JpaPredicate... predicates) {
        Predicate[] mappedPredicates = stream(predicates)
                .map(JpaPredicate::toRawPredicate)
                .toArray(Predicate[]::new);
        return () -> criteriaBuilder.or(mappedPredicates);
    }

    public JpaPredicate allMatch() {
        return and();
    }

    public JpaPredicate noneMatch() {
        return or();
    }
}
