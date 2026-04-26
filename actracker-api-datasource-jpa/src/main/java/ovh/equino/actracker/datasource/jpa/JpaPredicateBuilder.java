package ovh.equino.actracker.datasource.jpa;

import jakarta.persistence.criteria.*;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder.PageableAttribute.Direction;
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

    public static final String LOWEST_STRING = "";
    public static final String GREATEST_STRING = String.valueOf(Character.MAX_VALUE);

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

        var pageableAttributes = pageId.values().stream()
                .map(this::toPageableAttribute)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        return isInPage(pageableAttributes);
    }

    private JpaPredicate isInPage(
            List<? extends PageableAttribute<? extends Comparable<?>>> pageableValues) {

        if (isEmpty(pageableValues)) {
            return allMatch();
        }

        var predicates = new LinkedList<JpaPredicate>();
        var pageableValueIterator = pageableValues.iterator();
        var alreadyHandledValues = new ArrayList<PageableAttribute<?>>();

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

    private <T extends Comparable<T>> JpaPredicate predicateForNextValue(PageableAttribute<T> pageableAttribute,
                                                                         boolean isLast,
                                                                         Collection<PageableAttribute<?>> handledValues) {

        var handlePredicates = handledValues.stream()
                .map(value -> (JpaPredicate) () -> criteriaBuilder.equal(value.field(), value.value()));

        var newPredicate = switch (pageableAttribute.direction()) {
            case ASC -> isLast
                    ? (JpaPredicate) () -> criteriaBuilder.greaterThan(pageableAttribute.field, pageableAttribute.value)
                    : (JpaPredicate) () -> criteriaBuilder.greaterThanOrEqualTo(pageableAttribute.field, pageableAttribute.value);
            case DESC -> isLast
                    ? (JpaPredicate) () -> criteriaBuilder.lessThan(pageableAttribute.field, pageableAttribute.value)
                    : (JpaPredicate) () -> criteriaBuilder.lessThanOrEqualTo(pageableAttribute.field, pageableAttribute.value);
        };

        var predicates = concat(handlePredicates, Stream.of(newPredicate)).toArray(JpaPredicate[]::new);

        return and(predicates);
    }

    private Optional<PageableAttribute<? extends Comparable<?>>> toPageableAttribute(
            EntitySearchPageId.Value pageAttribute) {

        return commonPageableAttribute(pageAttribute)
                .or(() -> entityPageableAttribute(pageAttribute))
                .or(Optional::empty);
    }

    private Optional<PageableAttribute<? extends Comparable<?>>> commonPageableAttribute(
            EntitySearchPageId.Value pageAttribute) {

        if (pageAttribute.sortField() instanceof EntitySortCriteria.CommonField commonField) {
            return switch (commonField) {
                case ID -> Optional.of(PageableAttribute.of(
                        root.get(JpaEntity_.id),
                        pageAttribute.value().toString(),
                        Direction.from(pageAttribute.sortOrder()))
                );
            };
        }
        return Optional.empty();
    }

    protected abstract Optional<PageableAttribute<? extends Comparable<?>>> entityPageableAttribute(
            EntitySearchPageId.Value pageValue);

    protected record PageableAttribute<T extends Comparable<T>>(Expression<T> field,
                                                                T value,
                                                                Direction direction) {

        public static <T extends Comparable<T>> PageableAttribute<T> of(Expression<T> field,
                                                                        T value,
                                                                        Direction direction) {

            return new PageableAttribute<>(field, value, direction);
        }

        public enum Direction {
            DESC,
            ASC;

            public static Direction from(EntitySortCriteria.Order sortOrder) {
                return switch (sortOrder) {
                    case ASC -> ASC;
                    case DESC -> DESC;
                };
            }
        }
    }
}
