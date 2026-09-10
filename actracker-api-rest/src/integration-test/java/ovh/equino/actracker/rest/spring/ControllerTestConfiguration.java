package ovh.equino.actracker.rest.spring;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import ovh.equino.actracker.application.activity.ActivityApplicationService;
import ovh.equino.actracker.application.dashboard.DashboardApplicationService;
import ovh.equino.actracker.application.tag.TagApplicationService;
import ovh.equino.actracker.application.tagset.TagSetApplicationService;

@SpringBootConfiguration
@ComponentScan(basePackages = "ovh.equino.actracker.rest.spring")
class ControllerTestConfiguration {

    @MockBean
    private ActivityApplicationService activityApplicationService;

    @MockBean
    private DashboardApplicationService dashboardApplicationService;

    @MockBean
    private TagApplicationService tagApplicationService;

    @MockBean
    private TagSetApplicationService tagSetApplicationService;
}
