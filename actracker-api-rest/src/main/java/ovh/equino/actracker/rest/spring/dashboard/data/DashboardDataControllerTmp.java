package ovh.equino.actracker.rest.spring.dashboard.data;

import org.springframework.web.bind.annotation.*;
import ovh.equino.actracker.application.dashboard.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/dashboard/{dashboardId}/data")
class DashboardDataControllerTmp {

    private final DashboardApplicationService dashboardApplicationService;
    private final DashboardDataMapper mapper = new DashboardDataMapper();

    DashboardDataControllerTmp(DashboardApplicationService dashboardApplicationService) {

        this.dashboardApplicationService = dashboardApplicationService;
    }

    @RequestMapping(method = GET)
    @ResponseStatus(OK)
    DashboardData getDashboardData(@PathVariable("dashboardId") String dashboardId,
                                   @RequestParam(name = "rangeStartMillis", required = false) Long rangeStartMillis,
                                   @RequestParam(name = "rangeEndMillis", required = false) Long rangeEndMillis,
                                   @RequestParam(name = "requiredTags", required = false) String requiredTags
    ) {

        GenerateDashboardQuery generateDashboardQuery = new GenerateDashboardQuery(
                UUID.fromString(dashboardId),
                mapper.timestampToInstant(rangeStartMillis),
                mapper.timestampToInstant(rangeEndMillis),
                mapper.parseIds(requiredTags)
        );
        DashboardGenerationResult dashboardData =
                dashboardApplicationService.generateDashboard(generateDashboardQuery);

        return toResponse(dashboardData);
    }

    private DashboardData toResponse(DashboardGenerationResult dashboardGenerationResult) {
        List<DashboardDataChart> charts = dashboardGenerationResult.charts().stream()
                .map(this::toChartData)
                .toList();
        return new DashboardData(dashboardGenerationResult.name(), charts);
    }

    private DashboardDataChart toChartData(GeneratedChart generatedChart) {
        List<DashboardDataBucket> buckets = generatedChart.buckets().stream()
                .map(this::toBucketData)
                .toList();
        return new DashboardDataChart(generatedChart.name(), buckets);
    }

    private DashboardDataBucket toBucketData(GeneratedBucket generatedBucket) {
        return new DashboardDataBucket(
                generatedBucket.id(),
                mapper.instantToTimestamp(generatedBucket.rangeStart()),
                mapper.instantToTimestamp(generatedBucket.rangeEnd()),
                generatedBucket.bucketType(),
                generatedBucket.value(),
                generatedBucket.percentage(),
                generatedBucket.buckets().stream()
                        .map(this::toBucketData)
                        .toList()
        );
    }
}
