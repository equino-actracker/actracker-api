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
                .map(level -> toSortCriterion(level.field(), level.order()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<JpaSortCriteria> toSortCriterion(EntitySortCriteria.Field field, EntitySortCriteria.Order order) {
        return sortableField(field).flatMap(sortableField ->
                switch (order) {
                    case ASC -> Optional.of(() -> criteriaBuilder.asc(sortableField));
                    case DESC -> Optional.of(() -> criteriaBuilder.desc(sortableField));
                }
        );
    }

    private Optional<Expression<?>> sortableField(EntitySortCriteria.Field field) {
        return commonSortableField(field)
                .or(() -> entitySortableField(field))
                .or(Optional::empty);
    }

    private Optional<Expression<?>> commonSortableField(EntitySortCriteria.Field field) {
        if (field instanceof EntitySortCriteria.CommonField commonField) {
            return switch (commonField) {
                case ID -> Optional.of(root.get(JpaEntity_.id));
            };
        }
        return Optional.empty();
    }

    protected abstract Optional<Expression<?>> entitySortableField(EntitySortCriteria.Field field);

    public JpaPredicate isInPage(EntitySearchPageId pageId) {
        if (pageId.isEmpty()) {
            return allMatch();
        }

        var pageableValues = pageId.values().stream()
                .map(this::toPageableValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        // TODO remove:
        var pagePredicates = pageId.values().stream()
                .map(this::toPageableValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::isAfterPageValueBoundary)
                .toArray(JpaPredicate[]::new);

        return and(pagePredicates);
    }

    private JpaPredicate isAfterPageValueBoundary(List<PageableValue<?>> pageableValues) {
        if (isEmpty(pageableValues)) {
            return allMatch();
        }

        var predicates = new LinkedList<JpaPredicate>();
        var pageableValueIterator = pageableValues.iterator();
        var alreadyHandledFields = new ArrayList<PageableValue<?>>();

        while (pageableValueIterator.hasNext()) {
            predicates.add(
                    predicateForNextValue(
                            pageableValueIterator.next(),
                            pageableValueIterator.hasNext(),
                            alreadyHandledFields
                    )
            );
        }

        return or(predicates.toArray(new JpaPredicate[]{}));
    }

    private <T extends Comparable<T>> JpaPredicate predicateForNextValue(PageableValue<T> pageableValue,
                                                                         boolean isLast,
                                                                         Collection<PageableValue<?>> handledValues) {

        var handlePredicates = handledValues.stream()
                .map(value -> (JpaPredicate) () -> criteriaBuilder.equal(value.field(), value.value()));

        var newPredicate = isLast
                ? (JpaPredicate) () -> criteriaBuilder.greaterThanOrEqualTo(pageableValue.field, pageableValue.value)
                : (JpaPredicate) () -> criteriaBuilder.greaterThan(pageableValue.field, pageableValue.value);

        var predicates = concat(handlePredicates, Stream.of(newPredicate)).toArray(JpaPredicate[]::new);

        return or(predicates);
    }

    // TODO doesn't work:
    private <T extends Comparable<T>> JpaPredicate isAfterPageValueBoundary(PageableValue<T> pageableValue) {
        return switch (pageableValue.pagingDirection()) {
            case SKIP_LESSER -> () ->
                    criteriaBuilder.greaterThanOrEqualTo(pageableValue.field(), pageableValue.value());
            case SKIP_GREATER -> () ->
                    // TODO descending sort doesn't work yet
                    criteriaBuilder.lessThanOrEqualTo(pageableValue.field(), pageableValue.value());
        };
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
