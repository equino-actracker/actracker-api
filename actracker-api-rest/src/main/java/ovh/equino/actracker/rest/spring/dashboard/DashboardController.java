package ovh.equino.actracker.rest.spring.dashboard;

import org.springframework.web.bind.annotation.*;
import ovh.equino.actracker.application.dashboard.DashboardApplicationService;
import ovh.equino.actracker.application.dashboard.SearchDashboardsQuery;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.rest.spring.EntitySearchCriteriaBuilder;
import ovh.equino.actracker.rest.spring.SearchResponse;
import ovh.equino.actracker.rest.spring.share.Share;
import ovh.equino.actracker.rest.spring.share.ShareMapper;
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/dashboard")
class DashboardController {

    private final DashboardApplicationService dashboardApplicationService;
    private final IdentityProvider identityProvider;
    private final DashboardMapper dashboardMapper = new DashboardMapper();
    private final ChartMapper chartMapper = new ChartMapper();
    private final ShareMapper shareMapper = new ShareMapper();

    DashboardController(DashboardApplicationService dashboardApplicationService,
                        IdentityProvider identityProvider) {

        this.dashboardApplicationService = dashboardApplicationService;
        this.identityProvider = identityProvider;
    }

    @RequestMapping(method = GET, path = "/{id}")
    @ResponseStatus(OK)
    Dashboard getDashboard(@PathVariable("id") String id) {
        DashboardDto foundDashboard = dashboardApplicationService.getDashboard(UUID.fromString(id));
        return dashboardMapper.toResponse(foundDashboard);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(OK)
    Dashboard createDashboard(@RequestBody Dashboard dashboard) {
        DashboardDto dashboardDto = dashboardMapper.fromRequest(dashboard);
        DashboardDto createdDashboard = dashboardApplicationService.createDashboard(dashboardDto);

        return dashboardMapper.toResponse(createdDashboard);
    }

    @RequestMapping(method = GET, path = "/matching")
    @ResponseStatus(OK)
    SearchResponse<Dashboard> searchDashboards(
            @RequestParam(name = "pageId", required = false) String pageId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "term", required = false) String term,
            @RequestParam(name = "excludedDashboards", required = false) String excludedDashboards) {

        SearchDashboardsQuery searchDashboardsQuery = new SearchDashboardsQuery(
                pageSize,
                pageId,
                term,
                dashboardMapper.parseIds(excludedDashboards)
        );

        EntitySearchResult<DashboardDto> searchResult = dashboardApplicationService.searchDashboards(searchDashboardsQuery);
        return dashboardMapper.toResponse(searchResult);
    }

    @RequestMapping(method = DELETE, path = "/{id}")
    @ResponseStatus(OK)
    void deleteDashboard(@PathVariable("id") String id) {
        dashboardApplicationService.deleteDashboard(UUID.fromString(id));
    }

    @RequestMapping(method = POST, path = "/{id}/share")
    @ResponseStatus(OK)
    Dashboard shareDashboard(
            @PathVariable("id") String id,
            @RequestBody Share share) {

        ovh.equino.actracker.domain.share.Share newShare = shareMapper.fromRequest(share);

        DashboardDto sharedDashboard = dashboardApplicationService.shareDashboard(newShare, UUID.fromString(id));
        return dashboardMapper.toResponse(sharedDashboard);
    }

    @RequestMapping(method = DELETE, path = "/{id}/share/{granteeName}")
    @ResponseStatus(OK)
    Dashboard unshareDashboard(@PathVariable("id") String dashboardId, @PathVariable("granteeName") String granteeName) {
        DashboardDto unsharedDashboard = dashboardApplicationService.unshareDashboard(granteeName, UUID.fromString(dashboardId));
        return dashboardMapper.toResponse(unsharedDashboard);
    }

    @RequestMapping(method = PUT, path = "/{id}/name")
    @ResponseStatus(OK)
    Dashboard renameDashboard(@PathVariable("id") String dashboardId, @RequestBody String newName) {
        DashboardDto renamedDashboard = dashboardApplicationService.renameDashboard(newName, UUID.fromString(dashboardId));

        return dashboardMapper.toResponse(renamedDashboard);
    }

    @RequestMapping(method = POST, path = "/{dashboardId}/chart")
    @ResponseStatus(OK)
    Dashboard addChart(@PathVariable("dashboardId") String dashboardId, @RequestBody Chart chart) {
        ovh.equino.actracker.domain.dashboard.Chart newChart = chartMapper.fromRequest(chart);

        DashboardDto updatedDashboard = dashboardApplicationService
                .addChart(newChart, UUID.fromString(dashboardId));

        return dashboardMapper.toResponse(updatedDashboard);
    }

    @RequestMapping(method = DELETE, path = "/{dashboardId}/chart/{chartId}")
    @ResponseStatus(OK)
    Dashboard deleteChart(@PathVariable("dashboardId") String dashboardId, @PathVariable("chartId") String chartId) {
        DashboardDto updatedDashboard = dashboardApplicationService
                .deleteChart(UUID.fromString(chartId), UUID.fromString(dashboardId));

        return dashboardMapper.toResponse(updatedDashboard);

    }
}
