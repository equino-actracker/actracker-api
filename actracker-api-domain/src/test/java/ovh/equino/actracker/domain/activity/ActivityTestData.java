package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.tenant.TenantTestData;

import java.time.Instant;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.domain.tenant.TenantTestData.aTenant;

public final class ActivityTestData {

    private UUID id = randomUUID();
    private TenantTestData creator = aTenant();
    private String title = "titleless activity";
    private Instant startTime = null;
    private Instant endTime = null;
    // TODO startTime, endTime, comment, tags, metrics

    public static ActivityTestData anActivity() {
        return new ActivityTestData();
    }

    public ActivityTestData createdBy(TenantTestData creator) {
        this.creator = creator;
        return this;
    }

    public ActivityTestData withId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID id() {
        return id;
    }

    public ActivityTestData withTitle(String title) {
        this.title = title;
        return this;
    }

    public String title() {
        return title;
    }

    public ActivityTestData startedAt(Instant startTime) {
        this.startTime = startTime;
        return this;
    }

    public Instant startTime() {
        return startTime;
    }

    public ActivityTestData endedAt(Instant endTime) {
        this.endTime = endTime;
        return this;
    }

    public Instant endTime() {
        return endTime;
    }

    public ActivityDto asDto() {
        return new ActivityDto(id, creator.id(), title, startTime, endTime, null, emptySet(), emptyList(), false);
    }
}
