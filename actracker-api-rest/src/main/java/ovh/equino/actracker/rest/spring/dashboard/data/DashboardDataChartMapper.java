package ovh.equino.actracker.rest.spring.dashboard.data;

import ovh.equino.actracker.domain.dashboard.DashboardChartData;
import ovh.equino.actracker.rest.spring.PayloadMapper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

class DashboardDataChartMapper extends PayloadMapper {

    private final DashboardDataBucketMapper bucketMapper = new DashboardDataBucketMapper();

    DashboardDataChart toResponse(DashboardChartData chart) {
        return new DashboardDataChart(
                chart.name(),
                bucketMapper.toResponse(chart.buckets())
        );
    }

    Collection<DashboardDataChart> toResponse(Collection<DashboardChartData> charts) {
        return Objects.requireNonNullElse(charts, new LinkedList<DashboardChartData>()).stream()
                .map(this::toResponse)
                .toList();
    }

}
