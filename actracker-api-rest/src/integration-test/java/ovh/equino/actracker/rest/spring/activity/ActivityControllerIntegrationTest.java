package ovh.equino.actracker.rest.spring.activity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ovh.equino.actracker.application.activity.ActivityApplicationService;
import ovh.equino.actracker.domain.activity.ActivityTestData;
import ovh.equino.actracker.rest.spring.ControllerIntegrationTest;

import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ovh.equino.actracker.domain.activity.ActivityTestData.complexActivity;
import static ovh.equino.actracker.domain.activity.ActivityTestData.minimalActivity;
import static ovh.equino.actracker.rest.spring.activity.ActivityRestTestData.aRestfulActivity;

@WebMvcTest(ActivityController.class)
class ActivityControllerIntegrationTest implements ControllerIntegrationTest {

    private static final String ACTIVITY_URL = "/api/activity/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ActivityApplicationService activityApplicationService;

    @ParameterizedTest(name = "{0}")
    @MethodSource("activitiesToGet")
    void shouldGetActivity(String testName, ActivityTestData activityToGet) throws Exception {
        // given
        var restfulActivity = aRestfulActivity(activityToGet);
        when(activityApplicationService.getActivity(restfulActivity.id()))
                .thenReturn(restfulActivity.asActivityResult());

        // when / then
        mockMvc.perform(get(ACTIVITY_URL + restfulActivity.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(restfulActivity.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    private static Stream<Arguments> activitiesToGet() {
        return Stream.of(
                Arguments.of("Minimalistic activity", minimalActivity()),
                Arguments.of("Full activity", complexActivity())
        );
    }
}
