package ovh.equino.actracker.datasource.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.jpa.JpaEntity;
import ovh.equino.actracker.jpa.JpaEntity_;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toUnmodifiableSet;
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

    public JpaPredicate isInPage(EntitySearchPageId pageId) {
        if (pageId.isEmpty()) {
            return allMatch();
        }
        var pagePredicates = pageId.values().stream()
                .map(this::toPageableValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::isAfterPageValueBoundary)
                .toArray(JpaPredicate[]::new);

        return and(pagePredicates);
    }

    private <T extends Comparable<T>> JpaPredicate isAfterPageValueBoundary(PageableValue<T> pageableValue) {
        // TODO support DESC ordering
        return () -> criteriaBuilder.greaterThanOrEqualTo(pageableValue.field(), pageableValue.value());
    }

    private Optional<PageableValue<? extends Comparable<?>>> toPageableValue(EntitySearchPageId.Value pageValue) {
        return commonPageableValue(pageValue)
                .or(() -> entityPageableValue(pageValue))
                .or(Optional::empty);
    }

    private Optional<PageableValue<? extends Comparable<?>>> commonPageableValue(EntitySearchPageId.Value pageValue) {
        if (pageValue.sortField() instanceof EntitySortCriteria.CommonField commonField) {
            return switch (commonField) {
                case ID -> Optional.of(PageableValue.of(root.get(JpaEntity_.id), (String) pageValue.value()));
            };
        }
        return Optional.empty();
    }

    protected abstract Optional<PageableValue<? extends Comparable<?>>> entityPageableValue(
            EntitySearchPageId.Value pageValue);

    protected record PageableValue<T extends Comparable<T>>(Path<T> field, T value) {
        public static <T extends Comparable<T>> PageableValue<T> of(Path<T> field, T value) {
            return new PageableValue<>(field, value);
        }
    }
}
