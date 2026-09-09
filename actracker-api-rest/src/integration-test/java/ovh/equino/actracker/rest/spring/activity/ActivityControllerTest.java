package ovh.equino.actracker.rest.spring.activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ovh.equino.actracker.application.activity.ActivityApplicationService;
import ovh.equino.actracker.domain.activity.ActivityTestData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ovh.equino.actracker.rest.spring.activity.ActivityRestTestData.restfulActivity;

@WebMvcTest(ActivityController.class)
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ActivityApplicationService activityApplicationService;

    @Test
    void shouldGetActivity() throws Exception {
        // given
        var restfulActivity = restfulActivity();
        when(activityApplicationService.getActivity(restfulActivity.id()))
                .thenReturn(restfulActivity.asActivityResult());

        // when
        var mvcResult = mockMvc.perform(get("/api/activity/" + restfulActivity.id()))
                .andExpect(status().isOk())
                .andReturn();

        // then
//        var responseBodyJson = mvcResult.getResponse().getContentAsString();
//        var actualResponse = objectMapper.readValue(responseBodyJson, Activity.class);
//        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @SpringBootConfiguration
    static class TestConfiguration {

    }
}
