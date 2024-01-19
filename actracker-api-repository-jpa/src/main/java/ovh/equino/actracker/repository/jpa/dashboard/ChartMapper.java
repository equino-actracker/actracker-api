package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.domain.dashboard.AnalysisMetric;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.ChartId;
import ovh.equino.actracker.domain.dashboard.GroupBy;
import ovh.equino.actracker.jpa.dashboard.ChartEntity;
import ovh.equino.actracker.jpa.dashboard.DashboardEntity;
import ovh.equino.actracker.jpa.tag.TagEntity;

import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;

class ChartMapper {

    List<Chart> toDomainObjects(Collection<ChartEntity> entities) {
        return requireNonNullElse(entities, new ArrayList<ChartEntity>()).stream()
                .map(this::toDomainObject)
                .toList();
    }

    Chart toDomainObject(ChartEntity entity) {
        if(isNull(entity)) {
            return null;
        }
        Set<UUID> entityTags = requireNonNullElse(entity.getTags(), new HashSet<TagEntity>()).stream()
                .map(TagEntity::getId)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());
        return new Chart(
                new ChartId(entity.getId()),
                entity.getName(),
                GroupBy.valueOf(entity.getGroupBy()),
                AnalysisMetric.valueOf(entity.getMetric()),
                entityTags,
                entity.isDeleted()
        );
    }

    List<ChartEntity> toEntities(Collection<Chart> charts, DashboardEntity dashboard) {
        return requireNonNullElse(charts, new ArrayList<Chart>()).stream()
                .map(chart -> toEntity(chart, dashboard))
                .toList();
    }

    ChartEntity toEntity(Chart chart, DashboardEntity dashboard) {
        Set<TagEntity> dtoTags = requireNonNullElse(chart.includedTags(), new HashSet<UUID>()).stream()
                .map(UUID::toString)
                .map(this::toTagEntity)
                .collect(toUnmodifiableSet());

        ChartEntity entity = new ChartEntity();
        entity.setId(chart.id().toString());
        entity.setName(chart.name());
        entity.setDashboard(dashboard);
        entity.setGroupBy(chart.groupBy().toString());
        entity.setMetric(chart.analysisMetric().toString());
        entity.setTags(dtoTags);
        entity.setDeleted(chart.isDeleted());
        return entity;
    }

    private TagEntity toTagEntity(String tagId) {
        TagEntity tagEntity = new TagEntity();
        tagEntity.setId(tagId);
        return tagEntity;
    }
}
