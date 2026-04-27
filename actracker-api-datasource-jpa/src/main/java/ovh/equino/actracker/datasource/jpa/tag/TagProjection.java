package ovh.equino.actracker.datasource.jpa.tag;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDto;

import java.util.List;
import java.util.UUID;

record TagProjection(String id,
                     String creatorId,
                     String name,
                     String nameSortableAscending,
                     String nameSortableDescending,
                     String tagNameLowerCase,
                     Integer tagNameNullWeight,
                     Boolean deleted) {

    TagProjection(String id, String creatorId, String name, Boolean deleted) {
        this(id, creatorId, name, null, null, null, null, deleted);
    }

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
