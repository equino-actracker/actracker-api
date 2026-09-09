package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.tenant.TenantTestData;

import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.domain.tenant.TenantTestData.aTenant;

public final class TagTestData {

    private UUID id = randomUUID();
    private TenantTestData creator = aTenant();
    private String name = "nameless tag";

    public static TagTestData aTag() {
        return new TagTestData();
    }

    public TagTestData createdBy(TenantTestData creator) {
        this.creator = creator;
        return this;
    }

    public TagTestData withId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID id() {
        return id;
    }

    public TagTestData named(String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }

    public TagDto asDto() {
        return new TagDto(id, creator.id(), name, emptyList(), emptyList(), false);
    }
}
