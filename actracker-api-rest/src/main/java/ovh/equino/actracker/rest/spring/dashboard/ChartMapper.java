package ovh.equino.actracker.rest.spring.dashboard;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNullElse;
import static ovh.equino.actracker.domain.dashboard.Chart.*;

class ChartMapper {

    List<ovh.equino.actracker.domain.dashboard.Chart> fromRequest(List<Chart> chartsRequest) {
        return requireNonNullElse(chartsRequest, new ArrayList<Chart>()).stream()
                .map(this::fromRequest)
                .toList();
    }

    ovh.equino.actracker.domain.dashboard.Chart fromRequest(Chart chartRequest) {
        GroupBy groupBy = GroupBy.valueOf(chartRequest.groupBy().name());
        return new ovh.equino.actracker.domain.dashboard.Chart(chartRequest.name(), groupBy);
    }

    List<Chart> toResponse(List<ovh.equino.actracker.domain.dashboard.Chart> charts) {
        return requireNonNullElse(charts, new ArrayList<ovh.equino.actracker.domain.dashboard.Chart>()).stream()
                .map(this::toResponse)
                .toList();
    }

    Chart toResponse(ovh.equino.actracker.domain.dashboard.Chart chart) {
        Chart.GroupBy groupBy = Chart.GroupBy.valueOf(chart.groupBy().name());
        return new Chart(chart.name(), groupBy);
    }
}
