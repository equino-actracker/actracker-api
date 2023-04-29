package ovh.equino.actracker.rest.spring.dashboard.data;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ovh.equino.actracker.domain.dashboard.DashboardService;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import java.math.BigDecimal;
import java.util.UUID;

import static java.util.List.of;
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
    DashboardData getData(@PathVariable("id") String id) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        ovh.equino.actracker.domain.dashboard.DashboardData dashboardData =
                dashboardService.generateDashboard(UUID.fromString(id), requester);

        return mapper.toResponse(dashboardData);

//        return new DashboardData(
//                "Super dashboard",
//                of(
//                        new DashboardDataChart(
//                                "Chart 1",
//                                of(
//                                        new DashboardDataBucket("bucket1", new BigDecimal("20"), new BigDecimal("0.1")),
//                                        new DashboardDataBucket("bucket2", new BigDecimal("40"), new BigDecimal("0.2")),
//                                        new DashboardDataBucket("bucket3", new BigDecimal("140"), new BigDecimal("0.7"))
//                                )
//                        ),
//                        new DashboardDataChart(
//                                "Chart 2",
//                                of(
//                                        new DashboardDataBucket("bucket1", new BigDecimal("20"), new BigDecimal("0.1")),
//                                        new DashboardDataBucket("bucket2", new BigDecimal("40"), new BigDecimal("0.2")),
//                                        new DashboardDataBucket("bucket3", new BigDecimal("140"), new BigDecimal("0.7"))
//                                )
//                        ),
//                        new DashboardDataChart(
//                                "Chart 3",
//                                of(
//                                        new DashboardDataBucket("bucket1", new BigDecimal("20"), new BigDecimal("0.1")),
//                                        new DashboardDataBucket("bucket2", new BigDecimal("40"), new BigDecimal("0.2")),
//                                        new DashboardDataBucket("bucket3", new BigDecimal("140"), new BigDecimal("0.7"))
//                                )
//                        )
//                )
//        );
    }
}
