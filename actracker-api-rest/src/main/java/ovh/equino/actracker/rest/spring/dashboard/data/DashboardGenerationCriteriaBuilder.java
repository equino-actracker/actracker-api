package ovh.equino.actracker.rest.spring.dashboard.data;

import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationCriteria;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.rest.spring.PayloadMapper;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

final class DashboardGenerationCriteriaBuilder extends PayloadMapper {

    private UUID dashboardId;
    private User generator;
    private Instant timeRangeStart;
    private Instant timeRangeEnd;
    private Set<UUID> tags;

    DashboardGenerationCriteriaBuilder withDashboardId(String dashboardId) {
        this.dashboardId = UUID.fromString(dashboardId);
        return this;
    }

    DashboardGenerationCriteriaBuilder withGenerator(User generator) {
        this.generator = generator;
        return this;
    }

    DashboardGenerationCriteriaBuilder withTimeRangeStart(Long rangeStartMillis) {
        this.timeRangeStart = timestampToInstant(rangeStartMillis);
        return this;
    }

    DashboardGenerationCriteriaBuilder withTimeRangeEnd(Long rangeStartMillis) {
        this.timeRangeEnd = timestampToInstant(rangeStartMillis);
        return this;
    }

    public DashboardGenerationCriteriaBuilder withTagsJointWithComma(String jointTagIds) {
        this.tags = parseIds(jointTagIds);
        return this;
    }

    DashboardGenerationCriteria build() {
        return new DashboardGenerationCriteria(dashboardId, generator, timeRangeStart, timeRangeEnd, tags);
    }
}
