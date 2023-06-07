package ovh.equino.actracker.rest.spring.dashboard.data;

import org.springframework.web.bind.annotation.*;
import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationCriteria;
import ovh.equino.actracker.domain.dashboard.DashboardService;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/dashboard/{id}/data")
class DashboardDataController {

    private final DashboardService dashboardService;
    private final IdentityProvider identityProvider;
    private final DashboardDataMapper mapper = new DashboardDataMapper();

    DashboardDataController(DashboardService dashboardService, IdentityProvider identityProvider) {
        this.dashboardService = dashboardService;
        this.identityProvider = identityProvider;
    }

    @RequestMapping(method = GET)
    @ResponseStatus(OK)
    DashboardData getData(
            @PathVariable("id") String id,
            @RequestParam(name = "rangeStartMillis", required = false) Long rangeStartMillis,
            @RequestParam(name = "rangeEndMillis", required = false) Long rangeEndMillis,
            @RequestParam(name = "requiredTags", required = false) String requiredTags
    ) {

        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        DashboardGenerationCriteria dashboardGenerationCriteria = new DashboardGenerationCriteriaBuilder()
                .withGenerator(requester)
                .withTimeRangeStart(rangeStartMillis)
                .withTimeRangeEnd(rangeEndMillis)
                .withTagsJointWithComma(requiredTags)
                .withDashboardId(id)
                .build();

        ovh.equino.actracker.domain.dashboard.generation.DashboardData dashboardData =
                dashboardService.generateDashboard(dashboardGenerationCriteria);

        return mapper.toResponse(dashboardData);
    }
}
