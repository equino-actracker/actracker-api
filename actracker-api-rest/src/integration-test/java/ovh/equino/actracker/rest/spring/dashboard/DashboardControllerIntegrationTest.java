package ovh.equino.actracker.rest.spring.dashboard;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ovh.equino.actracker.application.dashboard.DashboardApplicationService;
import ovh.equino.actracker.domain.dashboard.DashboardTestData;
import ovh.equino.actracker.rest.spring.ControllerIntegrationTest;

import java.util.stream.Stream;

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
                .thenReturn(restfulDashboard.asDashboardResult());

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
                .thenReturn(restfulDashboard.asDashboardResult());

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
}
