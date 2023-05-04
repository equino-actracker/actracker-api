package ovh.equino.actracker.rest.spring;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

public final class EntitySearchCriteriaBuilder extends PayloadMapper {

    private User searcher;
    private Integer pageSize;
    private String pageId;
    private String term;
    private Instant timeRangeStart;
    private Instant timeRangeEnd;
    private Set<UUID> excludeFilter;
    private final Deque<EntitySortCriteria.Level> sortLevels = new LinkedList<>();

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

    public EntitySearchCriteriaBuilder withSortLevel(EntitySortCriteria.Field field, EntitySortCriteria.Order order) {
        this.sortLevels.addLast(new EntitySortCriteria.Level(field, order));
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
