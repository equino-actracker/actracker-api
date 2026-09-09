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
import ovh.equino.actracker.application.activity.ActivityResult;
import ovh.equino.actracker.application.activity.MetricValueResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        var activityId = randomUUID();
        var expectedActivity = new ActivityResult(
                activityId,
                "dummyActivity",
                Instant.now(),
                Instant.now(),
                "comment",
                Set.of(randomUUID(), randomUUID()),
                List.of(
                        new MetricValueResult(randomUUID(), BigDecimal.ZERO),
                        new MetricValueResult(randomUUID(), BigDecimal.ZERO)
                )
        );
        when(activityApplicationService.getActivity(activityId)).thenReturn(expectedActivity);

        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/"))
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
