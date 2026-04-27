package ovh.equino.actracker.datasource.jpa;

import jakarta.persistence.criteria.*;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder.PageableAttribute.Relation;
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
import static ovh.equino.actracker.domain.EntitySortCriteria.Order.DESC;

public abstract class JpaPredicateBuilder<E extends JpaEntity> {

    // TODO remove
    public static final String LOWEST_STRING = "";
    // TODO remove
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
                .map(this::toOrderCriteria)
                .flatMap(List::stream)
                .toList();
    }

    private List<JpaSortCriteria> toOrderCriteria(EntitySortCriteria.Level sortCriterion) {
        var commonOrderCriteria = toCommonOrderCriteria(sortCriterion);
        if (isNotEmpty(commonOrderCriteria)) {
            return commonOrderCriteria;
        }
        return toEntityOrderCriteria(sortCriterion);
    }

    private List<JpaSortCriteria> toCommonOrderCriteria(EntitySortCriteria.Level sortCriterion) {
        if (sortCriterion.field() instanceof EntitySortCriteria.CommonField commonField) {
            return switch (commonField) {
                case ID -> DESC == sortCriterion.order()
                        ? singletonList(() -> criteriaBuilder.desc(root.get(JpaEntity_.id)))
                        : singletonList(() -> criteriaBuilder.asc(root.get(JpaEntity_.id)));
            };
        }
        return emptyList();
    }

    protected abstract List<JpaSortCriteria> toEntityOrderCriteria(EntitySortCriteria.Level sortCriterion);

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

        var pageConditions = pageId.values().stream()
                .map(this::toPageConditions)
                .flatMap(List::stream)
                .toList();

        return isInPage(pageConditions);
    }

    private JpaPredicate isInPage(
            List<? extends PageableAttribute<? extends Comparable<?>>> pageConditions) {

        if (isEmpty(pageConditions)) {
            return allMatch();
        }

        var predicates = new LinkedList<JpaPredicate>();
        var pageConditionsIterator = pageConditions.iterator();
        var alreadyHandledConditions = new ArrayList<PageableAttribute<?>>();

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

    private <T extends Comparable<T>> JpaPredicate predicateForNextCondition(PageableAttribute<T> pageCondition,
                                                                             boolean isLast,
                                                                             Collection<PageableAttribute<?>> handledConditions) {

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

    private List<PageableAttribute<? extends Comparable<?>>> toPageConditions(EntitySearchPageId.Value pageAttribute) {
        var commonPageConditions = toCommonPageConditions(pageAttribute);
        if (isNotEmpty(commonPageConditions)) {
            return commonPageConditions;
        }
        return toEntityPageConditions(pageAttribute);
    }

    private List<PageableAttribute<? extends Comparable<?>>> toCommonPageConditions(
            EntitySearchPageId.Value pageAttribute) {

        if (pageAttribute.sortField() instanceof EntitySortCriteria.CommonField commonField) {
            return switch (commonField) {
                case ID -> singletonList(PageableAttribute.of(
                        root.get(JpaEntity_.id),
                        pageAttribute.value().toString(),
                        Relation.from(pageAttribute.sortOrder()))
                );
            };
        }
        return emptyList();
    }

    protected abstract List<PageableAttribute<? extends Comparable<?>>> toEntityPageConditions(
            EntitySearchPageId.Value pageAttribute);

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
                        Relation.from(pageAttribute.sortOrder()))
                );
            };
        }
        return Optional.empty();
    }

    protected abstract Optional<PageableAttribute<? extends Comparable<?>>> entityPageableAttribute(
            EntitySearchPageId.Value pageValue);

    // TODO rename to page condition
    protected record PageableAttribute<T extends Comparable<T>>(Expression<T> field,
                                                                T value,
                                                                Relation relation) {

        public static <T extends Comparable<T>> PageableAttribute<T> of(Expression<T> field,
                                                                        T value,
                                                                        Relation relation) {

            return new PageableAttribute<>(field, value, relation);
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
