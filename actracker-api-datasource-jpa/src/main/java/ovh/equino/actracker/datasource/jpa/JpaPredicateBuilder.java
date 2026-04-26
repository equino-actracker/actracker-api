package ovh.equino.actracker.datasource.jpa;

import jakarta.persistence.criteria.*;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder.PageableValue.PagingDirection;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.jpa.JpaEntity;
import ovh.equino.actracker.jpa.JpaEntity_;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static java.util.stream.Stream.concat;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
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

    public List<JpaSortCriteria> sortCriteria(EntitySortCriteria sortCriteria) {
        return sortCriteria.levels().stream()
                .map(this::toSortCriterion)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<JpaSortCriteria> toSortCriterion(EntitySortCriteria.Level sortCriterion) {
        return sortableAttribute(sortCriterion)
                .map(sortableAttribute -> () -> switch (sortCriterion.order()) {
                    case ASC -> criteriaBuilder.asc(sortableAttribute);
                    case DESC -> criteriaBuilder.desc(sortableAttribute);
                });
    }

    private Optional<Expression<?>> sortableAttribute(EntitySortCriteria.Level sortCriterion) {
        return commonSortableAttribute(sortCriterion)
                .or(() -> entitySortableAttribute(sortCriterion))
                .or(Optional::empty);
    }

    private Optional<Expression<?>> commonSortableAttribute(EntitySortCriteria.Level sortCriterion) {
        if (sortCriterion.field() instanceof EntitySortCriteria.CommonField commonField) {
            return switch (commonField) {
                case ID -> Optional.of(root.get(JpaEntity_.id));
            };
        }
        return Optional.empty();
    }

    protected abstract Optional<Expression<?>> entitySortableAttribute(EntitySortCriteria.Level sortCriterion);

    public JpaPredicate isInPage(EntitySearchPageId pageId) {
        if (pageId.isEmpty()) {
            return allMatch();
        }

        var pageableValues = pageId.values().stream()
                .map(this::toPageableValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        return isAfterPageValueBoundary(pageableValues);
    }

    private JpaPredicate isAfterPageValueBoundary(
            List<? extends PageableValue<? extends Comparable<?>>> pageableValues) {

        if (isEmpty(pageableValues)) {
            return allMatch();
        }

        var predicates = new LinkedList<JpaPredicate>();
        var pageableValueIterator = pageableValues.iterator();
        var alreadyHandledValues = new ArrayList<PageableValue<?>>();

        while (pageableValueIterator.hasNext()) {
            var pageableValue = pageableValueIterator.next();
            predicates.add(
                    predicateForNextValue(
                            pageableValue,
                            pageableValueIterator.hasNext(),
                            alreadyHandledValues
                    )
            );
            alreadyHandledValues.add(pageableValue);
        }

        return or(predicates.toArray(new JpaPredicate[]{}));
    }

    private <T extends Comparable<T>> JpaPredicate predicateForNextValue(PageableValue<T> pageableValue,
                                                                         boolean isLast,
                                                                         Collection<PageableValue<?>> handledValues) {

        var handlePredicates = handledValues.stream()
                .map(value -> (JpaPredicate) () -> criteriaBuilder.equal(value.field(), value.value()));

        var newPredicate = isLast
                ? (JpaPredicate) () -> criteriaBuilder.greaterThan(pageableValue.field, pageableValue.value)
                : (JpaPredicate) () -> criteriaBuilder.greaterThanOrEqualTo(pageableValue.field, pageableValue.value);

        var predicates = concat(handlePredicates, Stream.of(newPredicate)).toArray(JpaPredicate[]::new);

        return and(predicates);
    }

    private Optional<PageableValue<? extends Comparable<?>>> toPageableValue(EntitySearchPageId.Value pageValue) {
        return commonPageableValue(pageValue)
                .or(() -> entityPageableValue(pageValue))
                .or(Optional::empty);
    }

    private Optional<PageableValue<? extends Comparable<?>>> commonPageableValue(EntitySearchPageId.Value pageValue) {
        if (pageValue.sortField() instanceof EntitySortCriteria.CommonField commonField) {
            return switch (commonField) {
                case ID -> Optional.of(PageableValue.of(
                        root.get(JpaEntity_.id),
                        pageValue.value().toString(),
                        PagingDirection.from(pageValue.sortOrder()))
                );
            };
        }
        return Optional.empty();
    }

    protected abstract Optional<PageableValue<? extends Comparable<?>>> entityPageableValue(
            EntitySearchPageId.Value pageValue);

    protected record PageableValue<T extends Comparable<T>>(Expression<T> field,
                                                            T value,
                                                            PagingDirection pagingDirection) {

        public static <T extends Comparable<T>> PageableValue<T> of(Expression<T> field,
                                                                    T value,
                                                                    PagingDirection pagingDirection) {

            return new PageableValue<>(field, value, pagingDirection);
        }

        public enum PagingDirection {
            SKIP_GREATER,
            SKIP_LESSER;

            public static PagingDirection from(EntitySortCriteria.Order sortOrder) {
                return switch (sortOrder) {
                    case ASC -> SKIP_LESSER;
                    case DESC -> SKIP_GREATER;
                };
            }
        }
    }
}
