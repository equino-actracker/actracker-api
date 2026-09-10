package ovh.equino.actracker.rest.spring.activity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ovh.equino.actracker.application.activity.ActivityApplicationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ovh.equino.actracker.rest.spring.activity.ActivityRestTestData.aRestfulActivity;

@WebMvcTest(ActivityController.class)
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ActivityApplicationService activityApplicationService;

    @Test
    void shouldGetActivity() throws Exception {
        // given
        var restfulActivity = aRestfulActivity();
        when(activityApplicationService.getActivity(restfulActivity.id()))
                .thenReturn(restfulActivity.asActivityResult());

        // when
        var mvcResult = mockMvc.perform(get("/api/activity/" + restfulActivity.id()))
                .andExpect(status().isOk())
                .andReturn();

        // then
        var actualResponse = mvcResult.getResponse().getContentAsString();
        assertThat(actualResponse).isEqualTo(restfulActivity.asHttpResponse());
    }

}
