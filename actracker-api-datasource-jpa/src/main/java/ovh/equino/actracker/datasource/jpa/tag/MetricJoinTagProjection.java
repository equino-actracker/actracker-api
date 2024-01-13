package ovh.equino.actracker.datasource.jpa.tag;

import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.MetricType;

import java.util.UUID;

record MetricJoinTagProjection(
        String id,
        String creatorId,
        String name,
        String type,
        String tagId,
        Boolean deleted) {

    MetricDto toMetric() {
        return new MetricDto(
                UUID.fromString(id()),
                UUID.fromString(creatorId()),
                name(),
                MetricType.valueOf(type()),
                deleted()
        );
    }
}
