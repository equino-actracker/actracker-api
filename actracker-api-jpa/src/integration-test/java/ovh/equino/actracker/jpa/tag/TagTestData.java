package ovh.equino.actracker.jpa.tag;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;

public final class TagTestData {

    private UUID id = randomUUID();
    private UUID creatorId = randomUUID();
    private String name = "nameless tag";
    private Collection<MetricDto> metrics = new ArrayList<>();
    private List<Share> shares = new ArrayList<>();
    private boolean deleted = false;

    public static TagTestData aTag() {
        return new TagTestData();
    }

    public TagTestData createdBy(UUID creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    public UUID creatorId() {
        return creatorId;
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

    public Collection<MetricDto> metrics() {
        return metrics;
    }

    public List<Share> shares() {
        return shares;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public TagDto asDto() {
        return new TagDto(id, creatorId, name, metrics, shares, deleted);
    }
}
