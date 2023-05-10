package ovh.equino.actracker.rest.spring.dashboard;

import ovh.equino.actracker.rest.spring.PayloadMapper;

import java.util.*;

import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static ovh.equino.actracker.domain.dashboard.Chart.GroupBy;

class ChartMapper extends PayloadMapper {

    List<ovh.equino.actracker.domain.dashboard.Chart> fromRequest(List<Chart> chartsRequest) {
        return requireNonNullElse(chartsRequest, new ArrayList<Chart>()).stream()
                .map(this::fromRequest)
                .toList();
    }

    ovh.equino.actracker.domain.dashboard.Chart fromRequest(Chart chartRequest) {
        return new ovh.equino.actracker.domain.dashboard.Chart(
                chartRequest.name(),
                GroupBy.valueOf(chartRequest.groupBy().name()),
                stringsToUuids(chartRequest.includedTags())
        );
    }

    List<Chart> toResponse(List<ovh.equino.actracker.domain.dashboard.Chart> charts) {
        return requireNonNullElse(charts, new ArrayList<ovh.equino.actracker.domain.dashboard.Chart>()).stream()
                .map(this::toResponse)
                .toList();
    }

    Chart toResponse(ovh.equino.actracker.domain.dashboard.Chart chart) {
        return new Chart(
                chart.name(),
                Chart.GroupBy.valueOf(chart.groupBy().name()),
                uuidsToStrings(chart.includedTags())
        );
    }
}
