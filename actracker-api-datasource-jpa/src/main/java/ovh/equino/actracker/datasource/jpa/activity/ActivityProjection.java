package ovh.equino.actracker.datasource.jpa.activity;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.MetricValue;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;

record ActivityProjection(String id,
                          String creatorId,
                          String title,
                          String titleLowerCase,
                          Integer titleNullWeight,
                          Timestamp startTime,
                          Timestamp endTime,
                          Integer endTimeNullWeight,
                          String comment,
                          Boolean deleted) {

    @SuppressWarnings("unused")
    ActivityProjection(String id,
                       String creatorId,
                       String title,
                       Timestamp startTime,
                       Timestamp endTime,
                       String comment,
                       Boolean deleted) {

        this(id, creatorId, title, null, null, startTime, endTime, null, comment, deleted);
    }

    ActivityDto toActivity(Set<UUID> tagIds, List<MetricValue> metricValues) {

        return new ActivityDto(
                UUID.fromString(id()),
                UUID.fromString(creatorId()),
                title(),
                isNull(startTime()) ? null : startTime().toInstant(),
                isNull(endTime()) ? null : endTime().toInstant(),
                comment(),
                tagIds,
                metricValues,
                deleted()
        );
    }
}
