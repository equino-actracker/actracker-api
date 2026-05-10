package ovh.equino.actracker.jpa.activity;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.jpa.tenant.TenantTestData;

import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.jpa.tenant.TenantTestData.aTenant;

public final class ActivityTestData {

    private UUID id = randomUUID();
    private TenantTestData creator = aTenant();
    private String title = "titleless activity";

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

    public ActivityDto asDto() {
        return new ActivityDto(id, creator.id(), title, null, null, null, emptySet(), emptyList(), false);
    }
}
