package ovh.equino.actracker.datasource.jpa;

import jakarta.persistence.criteria.*;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder.PageCondition.Relation;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.jpa.JpaEntity;
import ovh.equino.actracker.jpa.JpaEntity_;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static java.util.stream.Stream.concat;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class JpaPredicateBuilder<E extends JpaEntity> {

    private final CriteriaBuilder criteriaBuilder;
    private final Root<E> root;

    protected JpaPredicateBuilder(CriteriaBuilder criteriaBuilder, Root<E> root) {
        this.criteriaBuilder = criteriaBuilder;
        this.root = root;
    }

    public JpaPredicate hasId(UUID id) {
        return () -> criteriaBuilder.equal(root.get(JpaEntity_.id), id.toString());
    }

    public JpaPredicate hasIdIn(Collection<UUID> ids) {
        Set<String> idsAsStrings = ids
                .stream()
                .map(UUID::toString)
                .collect(toUnmodifiableSet());
        return in(idsAsStrings, root.get(JpaEntity_.id));
    }

    protected <T> JpaPredicate in(Collection<T> values, Path<T> field) {
        if (isEmpty(values)) {
            return noneMatch();
        }
        CriteriaBuilder.In<Object> in = criteriaBuilder.in(field);
        values.stream()
                .collect(toUnmodifiableSet())
                .forEach(in::value);
        return () -> in;
    }

    public JpaPredicate isNotExcluded(Collection<UUID> excludedIds) {
        if (isEmpty(excludedIds)) {
            return allMatch();
        }
        Set<String> idsAsStrings = excludedIds
                .stream()
                .map(UUID::toString)
                .collect(toUnmodifiableSet());

        return not(in(idsAsStrings, root.get(JpaEntity_.id)));
    }

    protected JpaPredicate matchesTerm(String term, Path<String> field) {
        if (isBlank(term)) {
            return allMatch();
        }
        String matchingValue = term + "%";
        return () -> criteriaBuilder.like(field, matchingValue);
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

    public JpaPredicate isInPage(EntitySearchPageId pageId) {
        if (pageId.isEmpty()) {
            return allMatch();
        }

        var pageConditions = pageId.values().stream()
                .map(this::toPageConditions)
                .flatMap(List::stream)
                .toList();

        return isInPage(pageConditions);
    }

    private JpaPredicate isInPage(
            List<? extends PageCondition<? extends Comparable<?>>> pageConditions) {

        if (isEmpty(pageConditions)) {
            return allMatch();
        }

        var predicates = new LinkedList<JpaPredicate>();
        var pageConditionsIterator = pageConditions.iterator();
        var alreadyHandledConditions = new ArrayList<PageCondition<?>>();

        while (pageConditionsIterator.hasNext()) {
            var pageCondition = pageConditionsIterator.next();
            predicates.add(
                    predicateForNextCondition(
                            pageCondition,
                            pageConditionsIterator.hasNext(),
                            alreadyHandledConditions
                    )
            );
            alreadyHandledConditions.add(pageCondition);
        }

        return or(predicates.toArray(new JpaPredicate[]{}));
    }

    private <T extends Comparable<T>> JpaPredicate predicateForNextCondition(PageCondition<T> pageCondition,
                                                                             boolean isLast,
                                                                             Collection<PageCondition<?>> handledConditions) {

        var handledPredicates = handledConditions.stream()
                .map(value -> (JpaPredicate) () -> criteriaBuilder.equal(value.field(), value.value()));

        var newPredicate = switch (pageCondition.relation()) {
            case GTE -> isLast
                    ? (JpaPredicate) () -> criteriaBuilder.greaterThan(pageCondition.field, pageCondition.value)
                    : (JpaPredicate) () -> criteriaBuilder.greaterThanOrEqualTo(pageCondition.field, pageCondition.value);
            case LTE -> isLast
                    ? (JpaPredicate) () -> criteriaBuilder.lessThan(pageCondition.field, pageCondition.value)
                    : (JpaPredicate) () -> criteriaBuilder.lessThanOrEqualTo(pageCondition.field, pageCondition.value);
        };

        var predicates = concat(handledPredicates, Stream.of(newPredicate)).toArray(JpaPredicate[]::new);

        return and(predicates);
    }

    private List<PageCondition<? extends Comparable<?>>> toPageConditions(EntitySearchPageId.Value pageAttribute) {
        var commonPageConditions = toCommonPageConditions(pageAttribute);
        if (isNotEmpty(commonPageConditions)) {
            return commonPageConditions;
        }
        return toEntityPageConditions(pageAttribute);
    }

    private List<PageCondition<? extends Comparable<?>>> toCommonPageConditions(
            EntitySearchPageId.Value pageAttribute) {

        if (pageAttribute.sortField() instanceof EntitySortCriteria.CommonField commonField) {
            return switch (commonField) {
                case ID -> singletonList(PageCondition.of(
                        root.get(JpaEntity_.id),
                        pageAttribute.value().toString(),
                        Relation.from(pageAttribute.sortOrder()))
                );
            };
        }
        return emptyList();
    }

    protected abstract List<PageCondition<? extends Comparable<?>>> toEntityPageConditions(
            EntitySearchPageId.Value pageAttribute);


    protected record PageCondition<T extends Comparable<T>>(Expression<T> field,
                                                            T value,
                                                            Relation relation) {

        public static <T extends Comparable<T>> PageCondition<T> of(Expression<T> field,
                                                                    T value,
                                                                    Relation relation) {

            return new PageCondition<>(field, value, relation);
        }

        public enum Relation {
            LTE,
            GTE;

            public static Relation from(EntitySortCriteria.Order sortOrder) {
                return switch (sortOrder) {
                    case ASC -> GTE;
                    case DESC -> LTE;
                };
            }
        }
    }
}
