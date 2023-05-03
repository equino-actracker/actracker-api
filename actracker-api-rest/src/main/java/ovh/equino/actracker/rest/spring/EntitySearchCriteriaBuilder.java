package ovh.equino.actracker.rest.spring;

import org.apache.commons.lang3.StringUtils;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.lang3.StringUtils.split;

public final class EntitySearchCriteriaBuilder extends PayloadMapper {

    private User searcher;
    private Integer pageSize;
    private String pageId;
    private String term;
    private Instant timeRangeStart;
    private Instant timeRangeEnd;
    private Set<UUID> excludeFilter;

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

    public EntitySearchCriteria build() {
        return new EntitySearchCriteria(
                searcher,
                pageSize,
                pageId,
                term,
                timeRangeStart,
                timeRangeEnd,
                excludeFilter
        );
    }
}
