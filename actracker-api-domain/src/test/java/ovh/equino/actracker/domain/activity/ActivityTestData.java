package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.tenant.TenantTestData;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.domain.tenant.TenantTestData.aTenant;

public record ActivityTestData(UUID id,
                               TenantTestData creator,
                               String title,
                               Instant startTime,
                               Instant endTime,
                               String comment,
                               Set<UUID> tags,
                               List<MetricValue> metricValues,
                               boolean isDeleted) {

    public static ActivityTestData minimalActivity() {
        return new ActivityTestData(
                randomUUID(),
                aTenant(),
                null,
                null,
                null,
                null,
                emptySet(),
                emptyList(),
                false
        );
    }

    public static ActivityTestData complexActivity() {
        var id = randomUUID();
        return new ActivityTestData(
                id,
                aTenant(),
                "title " + id,
                now().minusSeconds(5),
                now().plusSeconds(5),
                "comment " + id,
                Set.of(randomUUID(), randomUUID()),
                List.of(new MetricValue(randomUUID(), ZERO), new MetricValue(randomUUID(), ONE)),
                false
        );
    }

    public ActivityTestData withId(UUID id) {
        return new ActivityTestData(
                id,
                this.creator,
                this.title,
                this.startTime,
                this.endTime,
                this.comment,
                this.tags,
                this.metricValues,
                this.isDeleted
        );
    }

    public ActivityTestData createdBy(TenantTestData creator) {
        return new ActivityTestData(
                this.id,
                creator,
                this.title,
                this.startTime,
                this.endTime,
                this.comment,
                this.tags,
                this.metricValues,
                this.isDeleted
        );
    }

    public ActivityTestData withTitle(String title) {
        return new ActivityTestData(
                this.id,
                this.creator,
                title,
                this.startTime,
                this.endTime,
                this.comment,
                this.tags,
                this.metricValues,
                this.isDeleted
        );
    }

    public ActivityTestData startedAt(Instant startTime) {
        return new ActivityTestData(
                this.id,
                this.creator,
                this.title,
                startTime,
                this.endTime,
                this.comment,
                this.tags,
                this.metricValues,
                this.isDeleted
        );
    }

    public ActivityTestData endedAt(Instant endTime) {
        return new ActivityTestData(
                this.id,
                this.creator,
                this.title,
                this.startTime,
                endTime,
                this.comment,
                this.tags,
                this.metricValues,
                this.isDeleted
        );
    }

    public ActivityDto asDto() {
        return new ActivityDto(id, creator.id(), title, startTime, endTime, comment, tags, metricValues, isDeleted);
    }
}
