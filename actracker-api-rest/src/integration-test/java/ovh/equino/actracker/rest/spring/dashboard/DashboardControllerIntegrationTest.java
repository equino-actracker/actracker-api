package ovh.equino.actracker.rest.spring.dashboard;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.application.SortCriteria;
import ovh.equino.actracker.application.dashboard.DashboardApplicationService;
import ovh.equino.actracker.application.dashboard.DashboardResult;
import ovh.equino.actracker.application.dashboard.SearchDashboardsQuery;
import ovh.equino.actracker.domain.dashboard.DashboardTestData;
import ovh.equino.actracker.rest.spring.ControllerIntegrationTest;
import ovh.equino.actracker.rest.spring.SearchRestTestData;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ovh.equino.actracker.domain.dashboard.DashboardTestData.complexDashboard;
import static ovh.equino.actracker.domain.dashboard.DashboardTestData.minimalDashboard;
import static ovh.equino.actracker.rest.spring.dashboard.DashboardRestTestData.aRestfulDashboard;

@WebMvcTest(DashboardController.class)
class DashboardControllerIntegrationTest implements ControllerIntegrationTest {

    private static final String DASHBOARD_URL = "/api/dashboard";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DashboardApplicationService dashboardApplicationService;

    @ParameterizedTest(name = "{0}")
    @MethodSource("dashboards")
    void shouldGetDashboard(String testName, DashboardTestData dashboardToGet) throws Exception {
        // given
        var restfulDashboard = aRestfulDashboard(dashboardToGet);
        when(dashboardApplicationService.getDashboard(dashboardToGet.id()))
                .thenReturn(restfulDashboard.asEntityResult());

        // when / then
        mockMvc.perform(get(DASHBOARD_URL + "/" + dashboardToGet.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(restfulDashboard.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dashboards")
    void shouldCreateDashboard(String testName, DashboardTestData dashboardToCreate) throws Exception {
        // given
        var restfulDashboard = aRestfulDashboard(dashboardToCreate);
        when(dashboardApplicationService.createDashboard(restfulDashboard.asCreateDashboardCommand()))
                .thenReturn(restfulDashboard.asEntityResult());

        // when / then
        mockMvc.perform(post(DASHBOARD_URL)
                        .contentType(APPLICATION_JSON)
                        .content(restfulDashboard.asHttpRequest()))
                .andExpect(status().isOk())
                .andExpect(content().json(restfulDashboard.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    private static Stream<Arguments> dashboards() {
        return Stream.of(
                Arguments.of("Minimalistic dashboard", minimalDashboard()),
                Arguments.of("Full dashboard", complexDashboard())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("searchDashboardsCriteria")
    void shouldSearchDashboardsMatchingCriteria(String testName,
                                                SearchDashboardsQuery expectedQuery,
                                                Map<String, String> searchCriteria)
            throws Exception {

        // given
        // TODO should not be necessary, mapping in controller should handle null
        when(dashboardApplicationService.searchDashboards(any(SearchDashboardsQuery.class)))
                .thenReturn(new SearchResult<>(null, List.of()));

        // when
        var requestBuilder = get(DASHBOARD_URL + "/matching");
        searchCriteria.forEach(requestBuilder::queryParam);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());

        // then
        verify(dashboardApplicationService).searchDashboards(expectedQuery);
    }

    private static Stream<Arguments> searchDashboardsCriteria() {
        return Stream.of(
                Arguments.of("empty criteria",
                        new SearchDashboardsQuery(null, null, null, null, null),
                        Map.of()),
                Arguments.of("pageSize filter",
                        new SearchDashboardsQuery(25, null, null, null, null),
                        Map.of("pageSize", "25")),
                Arguments.of("pageId filter",
                        new SearchDashboardsQuery(null, "page-id", null, null, null),
                        Map.of("pageId", "page-id")),
                Arguments.of("term filter",
                        new SearchDashboardsQuery(null, null, "gym", null, null),
                        Map.of("term", "gym")),
                Arguments.of("excludedDashboards filter",
                        new SearchDashboardsQuery(null, null, null, Set.of(new UUID(1L, 1L)), null),
                        Map.of("excludedDashboards", new UUID(1L, 1L).toString())),
                Arguments.of("orderBy filter",
                        new SearchDashboardsQuery(null, null, null, null,
                                new SortCriteria().orderBy("name", "desc")),
                        Map.of("orderBy", "name.desc")),
                Arguments.of("all filters combined",
                        new SearchDashboardsQuery(10, "p1", "run", Set.of(new UUID(2L, 2L)),
                                new SortCriteria().orderBy("name", "desc")),
                        Map.of("pageSize", "10",
                                "pageId", "p1",
                                "term", "run",
                                "excludedDashboards", new UUID(2L, 2L).toString(),
                                "orderBy", "name.desc"))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("matchingDashboards")
    void shouldFindMatchingDashboards(String testName, SearchRestTestData<DashboardResult> expectedSearchResult)
            throws Exception {

        // given
        when(dashboardApplicationService.searchDashboards(any(SearchDashboardsQuery.class)))
                .thenReturn(expectedSearchResult.asEntitySearchResult());

        // when / then
        mockMvc.perform(get(DASHBOARD_URL + "/matching"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        expectedSearchResult.asHttpResponse(),
                        NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER)
                );
    }

    private static Stream<Arguments> matchingDashboards() {
        return Stream.of(
                Arguments.of("no matching dashboards and no next page",
                        new SearchRestTestData<DashboardResult>(null, List.of())),
                Arguments.of("single minimal dashboard and no next page",
                        new SearchRestTestData<>(null, List.of(aRestfulDashboard(minimalDashboard())))),
                Arguments.of("three complex dashboards and next page",
                        new SearchRestTestData<>("next-page-id", List.of(
                                aRestfulDashboard(complexDashboard()),
                                aRestfulDashboard(complexDashboard()),
                                aRestfulDashboard(complexDashboard()))))
        );
    }
}
