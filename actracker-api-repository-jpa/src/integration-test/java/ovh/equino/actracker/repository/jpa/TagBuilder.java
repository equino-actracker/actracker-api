package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.MetricType;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;

import java.util.List;

import static java.util.Arrays.stream;
import static ovh.equino.actracker.repository.jpa.TestUtil.nextUUID;
import static ovh.equino.actracker.repository.jpa.TestUtil.randomString;

public final class TagBuilder {

    private TagDto newTag;

    TagBuilder(TenantDto creator) {
        this.newTag = new TagDto(
                nextUUID(),
                creator.id(),
                randomString(),
                List.of(
                        new MetricDto(nextUUID(), creator.id(), randomString(), MetricType.NUMERIC, false),
                        new MetricDto(nextUUID(), creator.id(), randomString(), MetricType.NUMERIC, false),
                        new MetricDto(nextUUID(), creator.id(), randomString(), MetricType.NUMERIC, false)
                ),
                List.of(
                        new Share(new User(nextUUID()), randomString()),
                        new Share(new User(nextUUID()), randomString())
                ),
                false
        );
    }

    public TagBuilder named(String name) {
        this.newTag = new TagDto(
                newTag.id(),
                newTag.creatorId(),
                name,
                newTag.metrics(),
                newTag.shares(),
                newTag.deleted()
        );
        return this;
    }

    public TagBuilder withMetrics(MetricDto... metrics) {
        this.newTag = new TagDto(
                newTag.id(),
                newTag.creatorId(),
                newTag.name(),
                stream(metrics).toList(),
                newTag.shares(),
                newTag.deleted()
        );
        return this;
    }

    public TagBuilder sharedWith(TenantDto... grantees) {
        this.newTag = new TagDto(
                newTag.id(),
                newTag.creatorId(),
                newTag.name(),
                newTag.metrics(),
                stream(grantees)
                        .map(grantee -> new Share(
                                        new User(grantee.id()),
                                        grantee.username()
                                )
                        )
                        .toList(),
                newTag.deleted()
        );
        return this;
    }

    public TagBuilder sharedWithNonExisting(String... granteeNames) {
        this.newTag = new TagDto(
                newTag.id(),
                newTag.creatorId(),
                newTag.name(),
                newTag.metrics(),
                stream(granteeNames)
                        .map(Share::new)
                        .toList(),
                newTag.deleted()
        );
        return this;

    }

    public TagBuilder deleted() {
        this.newTag = new TagDto(
                newTag.id(),
                newTag.creatorId(),
                newTag.name(),
                newTag.metrics(),
                newTag.shares(),
                true
        );
        return this;
    }

    public TagDto build() {
        return newTag;
    }
}
