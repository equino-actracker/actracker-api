package ovh.equino.actracker.rest.spring.dashboard;

import org.springframework.web.bind.annotation.*;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.DashboardService;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.rest.spring.EntitySearchCriteriaBuilder;
import ovh.equino.actracker.rest.spring.SearchResponse;
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/dashboard")
class DashboardController {

    private final DashboardService dashboardService;
    private final IdentityProvider identityProvider;
    private final DashboardMapper mapper = new DashboardMapper();

    DashboardController(DashboardService dashboardService, IdentityProvider identityProvider) {
        this.dashboardService = dashboardService;
        this.identityProvider = identityProvider;
    }

    @RequestMapping(method = GET, path = "/{id}")
    @ResponseStatus(OK)
    Dashboard getDashboard(@PathVariable("id") String id) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User requester = new User(requestIdentity.getId());

        DashboardDto foundDashboard = dashboardService.getDashboard(UUID.fromString(id), requester);
        return mapper.toResponse(foundDashboard);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(OK)
    Dashboard createDashboard(@RequestBody Dashboard dashboard) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User requester = new User(requestIdentity.getId());

        DashboardDto dashboardDto = mapper.fromRequest(dashboard);
        DashboardDto createdDashboard = dashboardService.createDashboard(dashboardDto, requester);

        return mapper.toResponse(createdDashboard);
    }

    @RequestMapping(method = PUT, path = "/{id}")
    @ResponseStatus(OK)
    Dashboard updateDashboard(@PathVariable("id") String id, @RequestBody Dashboard dashboard) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User requester = new User(requestIdentity.getId());

        DashboardDto dashboardDto = mapper.fromRequest(dashboard);
        DashboardDto updatedDashboard = dashboardService.updateDashboard(UUID.fromString(id), dashboardDto, requester);

        return mapper.toResponse(updatedDashboard);
    }

    @RequestMapping(method = GET, path = "/matching")
    @ResponseStatus(OK)
    SearchResponse<Dashboard> searchDashboards(
            @RequestParam(name = "pageId", required = false) String pageId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "term", required = false) String term,
            @RequestParam(name = "excludedDashboards", required = false) String excludedDashboards) {

        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        EntitySearchCriteria searchCriteria = new EntitySearchCriteriaBuilder()
                .withSearcher(requester)
                .withPageId(pageId)
                .withPageSize(pageSize)
                .withTerm(term)
                .withExcludedIdsJointWithComma(excludedDashboards)
                .build();

        EntitySearchResult<DashboardDto> searchResult = dashboardService.searchDashboards(searchCriteria);
        return mapper.toResponse(searchResult);
    }

    @RequestMapping(method = DELETE, path = "/{id}")
    @ResponseStatus(OK)
    void deleteDashboard(@PathVariable("id") String id) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User requester = new User(requestIdentity.getId());

        dashboardService.deleteDashboard(UUID.fromString(id), requester);
    }

    @RequestMapping(method = POST, path = "/{id}/share")
    @ResponseStatus(OK)
    Dashboard shareDashboard(
            @PathVariable("id") String id,
            @RequestBody Share share) {

        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        String granteeName = share.granteeName();

        DashboardDto sharedDashboard = dashboardService.shareDashboard(UUID.fromString(id), granteeName, requester);
        return mapper.toResponse(sharedDashboard);
    }
}
