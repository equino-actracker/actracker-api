package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.MetricValue;
import ovh.equino.actracker.domain.tenant.TenantDto;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.repository.jpa.TestUtil.randomBigDecimal;

public final class ActivityBuilder {

    private ActivityDto newActivity;

    ActivityBuilder(TenantDto creator) {
        this.newActivity = new ActivityDto(randomUUID(),
                creator.id(),
                randomUUID().toString(),
                Instant.ofEpochSecond(1),
                Instant.ofEpochSecond(2),
                randomUUID().toString(),
                Set.of(randomUUID(), randomUUID(), randomUUID()),
                List.of(
                        new MetricValue(randomUUID(), randomBigDecimal()),
                        new MetricValue(randomUUID(), randomBigDecimal()),
                        new MetricValue(randomUUID(), randomBigDecimal())
                ),
                false
        );
    }

    public ActivityDto build() {
        return newActivity;
    }
}
