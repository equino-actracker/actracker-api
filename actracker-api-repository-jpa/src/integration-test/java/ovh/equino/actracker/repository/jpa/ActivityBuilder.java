package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.MetricValue;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tenant.TenantDto;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static ovh.equino.actracker.repository.jpa.TestUtil.randomBigDecimal;
import static ovh.equino.actracker.repository.jpa.TestUtil.randomString;

public final class ActivityBuilder {

    private ActivityDto newActivity;

    ActivityBuilder(TenantDto creator) {
        this.newActivity = new ActivityDto(
                randomUUID(),
                creator.id(),
                randomString(),
                Instant.ofEpochSecond(1),
                Instant.ofEpochSecond(2),
                randomString(),
                Set.of(randomUUID(), randomUUID(), randomUUID()),
                List.of(
                        new MetricValue(randomUUID(), randomBigDecimal()),
                        new MetricValue(randomUUID(), randomBigDecimal()),
                        new MetricValue(randomUUID(), randomBigDecimal())
                ),
                false
        );
    }

    public ActivityBuilder deleted() {
        this.newActivity = new ActivityDto(
                newActivity.id(),
                newActivity.creatorId(),
                newActivity.title(),
                newActivity.startTime(),
                newActivity.endTime(),
                newActivity.comment(),
                newActivity.tags(),
                newActivity.metricValues(),
                true
        );
        return this;
    }

    public ActivityBuilder withTags(TagDto... tags) {
        this.newActivity = new ActivityDto(
                newActivity.id(),
                newActivity.creatorId(),
                newActivity.title(),
                newActivity.startTime(),
                newActivity.endTime(),
                newActivity.comment(),
                stream(tags).map(TagDto::id).collect(toUnmodifiableSet()),
                newActivity.metricValues(),
                newActivity.deleted()
        );
        return this;
    }

    public ActivityBuilder withMetricValues(MetricValue... values) {
        this.newActivity = new ActivityDto(
                newActivity.id(),
                newActivity.creatorId(),
                newActivity.title(),
                newActivity.startTime(),
                newActivity.endTime(),
                newActivity.comment(),
                newActivity.tags(),
                stream(values).toList(),
                newActivity.deleted()
        );
        return this;
    }

    public ActivityDto build() {
        return newActivity;
    }
}
