package ovh.equino.actracker.rest.spring.dashboard.data;

import org.springframework.web.bind.annotation.*;
import ovh.equino.actracker.application.dashboard.DashboardApplicationService;
import ovh.equino.actracker.application.dashboard.GenerateDashboardQuery;

import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/dashboard/{id}/data")
class DashboardDataController {

    private final DashboardApplicationService dashboardApplicationService;
    private final DashboardDataMapper mapper = new DashboardDataMapper();

    DashboardDataController(DashboardApplicationService dashboardApplicationService) {

        this.dashboardApplicationService = dashboardApplicationService;
    }

    @RequestMapping(method = GET)
    @ResponseStatus(OK)
    DashboardData getData(
            @PathVariable("id") String id,
            @RequestParam(name = "rangeStartMillis", required = false) Long rangeStartMillis,
            @RequestParam(name = "rangeEndMillis", required = false) Long rangeEndMillis,
            @RequestParam(name = "requiredTags", required = false) String requiredTags
    ) {

        GenerateDashboardQuery generateDashboardQuery = new GenerateDashboardQuery(
                UUID.fromString(id),
                mapper.timestampToInstant(rangeStartMillis),
                mapper.timestampToInstant(rangeEndMillis),
                mapper.parseIds(requiredTags)
        );

        ovh.equino.actracker.domain.dashboard.generation.DashboardData dashboardData =
                dashboardApplicationService.generateDashboard(generateDashboardQuery);

        return mapper.toResponse(dashboardData);
    }
}
