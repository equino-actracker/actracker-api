package ovh.equino.actracker.rest.spring.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ovh.equino.actracker.application.dashboard.DashboardApplicationService;
import ovh.equino.actracker.rest.spring.ControllerIntegrationTest;

import static org.assertj.core.api.Fail.fail;

@WebMvcTest(DashboardController.class)
class DashboardControllerIntegrationTest implements ControllerIntegrationTest {

    private static final String DASHBOARD_URL = "/api/dashboard";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DashboardApplicationService dashboardApplicationService;

    @Test
    void shouldGetDashboard() {
        fail("Not yet implemented");
    }
}
