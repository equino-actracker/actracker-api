package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.domain.dashboard.Chart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNullElse;

class ChartMapper {

    List<Chart> toValueObjects(Collection<ChartEntity> entities) {
        return requireNonNullElse(entities, new ArrayList<ChartEntity>()).stream()
                .map(this::toValueObject)
                .toList();
    }

    Chart toValueObject(ChartEntity entity) {
        return new Chart(entity.name, Chart.GroupBy.valueOf(entity.groupBy));
    }

    List<ChartEntity> toEntities(Collection<Chart> charts, DashboardEntity dashboard) {
        return requireNonNullElse(charts, new ArrayList<Chart>()).stream()
                .map(chart -> toEntity(chart, dashboard))
                .toList();
    }

    ChartEntity toEntity(Chart chart, DashboardEntity dashboard) {
        ChartEntity entity = new ChartEntity();
        entity.id = UUID.randomUUID().toString();
        entity.name = chart.name();
        entity.dashboard = dashboard;
        entity.groupBy = chart.groupBy().toString();
        return entity;
    }
}
