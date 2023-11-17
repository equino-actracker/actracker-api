package ovh.equino.actracker.repository.jpa.tag;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDto;

import java.util.List;
import java.util.UUID;

record TagProjection(String id, String creatorId, String name, Boolean deleted) {

    TagDto toTag(List<Share> shares, List<MetricDto> metrics) {
        return new TagDto(
                UUID.fromString(id()),
                UUID.fromString(creatorId()),
                name(),
                metrics,
                shares,
                deleted()
        );
    }
}
