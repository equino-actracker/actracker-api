package ovh.equino.actracker.rest.spring;

import org.apache.commons.lang3.StringUtils;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.*;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNullElse;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;

public final class EntitySearchCriteriaBuilder extends PayloadMapper {

    private User searcher;
    private Integer pageSize;
    private String pageId;
    private String term;
    private Instant timeRangeStart;
    private Instant timeRangeEnd;
    private Set<UUID> excludeFilter;
    private final Deque<EntitySortCriteria.Level> sortLevels = new LinkedList<>();

    private final Set<EntitySortCriteria.Field> possibleSortFields = new HashSet<>();

    public EntitySearchCriteriaBuilder withSearcher(User searcher) {
        this.searcher = searcher;
        return this;
    }

    public EntitySearchCriteriaBuilder withPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public EntitySearchCriteriaBuilder withPageId(String pageId) {
        this.pageId = pageId;
        return this;
    }

    public EntitySearchCriteriaBuilder withTerm(String term) {
        this.term = term;
        return this;
    }

    public EntitySearchCriteriaBuilder withTimeRangeStart(Instant timeRangeStart) {
        this.timeRangeStart = timeRangeStart;
        return this;
    }

    public EntitySearchCriteriaBuilder withTimeRangeEnd(Instant timeRangeEnd) {
        this.timeRangeEnd = timeRangeEnd;
        return this;
    }

    public EntitySearchCriteriaBuilder withExcludedIdsJointWithComma(String jointIds) {
        this.excludeFilter = parseIds(jointIds);
        return this;
    }

    public EntitySearchCriteriaBuilder withPossibleSortFields(EntitySortCriteria.Field... fields) {
        List<EntitySortCriteria.Field> notNullFields = stream(requireNonNullElse(fields, new EntitySortCriteria.Field[]{}))
                .filter(Objects::nonNull)
                .toList();
        this.possibleSortFields.addAll(notNullFields);
        return this;
    }

    public EntitySearchCriteriaBuilder withSortLevelsJointWithComma(String jointSortLevels) {
        if (possibleSortFields.isEmpty()) {
            throw new IllegalStateException("Possible sort fields must be declared first (withPossibleSortFields())");
        }

        String[] parsedSortLevels = requireNonNullElse(split(jointSortLevels, ','), new String[]{});

        stream(parsedSortLevels)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(this::toSortLevel)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEachOrdered(this::withSortLevel);

        return this;
    }

    private Optional<EntitySortCriteria.Level> toSortLevel(String sortLevel) {

        String[] sortLevelParts = split(sortLevel, '.');
        if (isBlank(sortLevelParts[0])) {
            return Optional.empty();
        }

        Optional<EntitySortCriteria.Field> sortField = toField(sortLevelParts);
        EntitySortCriteria.Order order = toOrder(sortLevelParts);

        return sortField.map(field -> new EntitySortCriteria.Level(field, order));
    }

    private Optional<EntitySortCriteria.Field> toField(String[] sortLevelParts) {
        return possibleSortFields.stream()
                .filter(field -> sortLevelParts[0].equalsIgnoreCase(field.toString()))
                .findFirst();
    }

    private static EntitySortCriteria.Order toOrder(String[] sortLevelParts) {
        List<String> possibleOrders = stream(EntitySortCriteria.Order.values())
                .map(Object::toString)
                .toList();
        return sortLevelParts.length > 1 && possibleOrders.contains(sortLevelParts[1].toUpperCase())
                ? EntitySortCriteria.Order.valueOf(sortLevelParts[1])
                : EntitySortCriteria.Order.ASC;
    }

    public EntitySearchCriteriaBuilder withSortLevel(EntitySortCriteria.Level sortLevel) {
        this.sortLevels.addLast(sortLevel);
        return this;
    }

    public EntitySearchCriteria build() {
        return new EntitySearchCriteria(
                searcher,
                pageSize,
                pageId,
                term,
                timeRangeStart,
                timeRangeEnd,
                excludeFilter,
                new EntitySortCriteria(sortLevels)
        );
    }
}
