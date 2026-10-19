package ovh.equino.actracker.rest.spring.activity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ovh.equino.actracker.application.activity.ActivityApplicationService;
import ovh.equino.actracker.domain.activity.ActivityTestData;
import ovh.equino.actracker.rest.spring.ControllerIntegrationTest;

import java.time.Instant;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ovh.equino.actracker.domain.activity.ActivityTestData.complexActivity;
import static ovh.equino.actracker.domain.activity.ActivityTestData.minimalActivity;
import static ovh.equino.actracker.rest.spring.PayloadUtils.jsonValue;
import static ovh.equino.actracker.rest.spring.activity.ActivityRestTestData.aRestfulActivity;

@WebMvcTest(ActivityController.class)
class ActivityControllerIntegrationTest implements ControllerIntegrationTest {

    private static final String ACTIVITY_URL = "/api/activity";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ActivityApplicationService activityApplicationService;

    @ParameterizedTest(name = "{0}")
    @MethodSource("activities")
    void shouldGetActivity(String testName, ActivityTestData activityToGet) throws Exception {
        // given
        var restfulActivity = aRestfulActivity(activityToGet);
        when(activityApplicationService.getActivity(activityToGet.id()))
                .thenReturn(restfulActivity.asActivityResult());

        // when / then
        mockMvc.perform(get(ACTIVITY_URL + "/" + activityToGet.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(restfulActivity.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("activities")
    void shouldCreateActivity(String testName, ActivityTestData activityToCreate) throws Exception {
        // given
        var restfulActivity = aRestfulActivity(activityToCreate);
        when(activityApplicationService.createActivity(restfulActivity.asCreateActivityCommand()))
                .thenReturn(restfulActivity.asActivityResult());

        // when / then
        mockMvc.perform(post(ACTIVITY_URL)
                        .contentType(APPLICATION_JSON)
                        .content(restfulActivity.asHttpRequest()))
                .andExpect(status().isOk())
                .andExpect(content().json(restfulActivity.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    private static Stream<Arguments> activities() {
        return Stream.of(
                Arguments.of("Minimalistic activity", minimalActivity()),
                Arguments.of("Full activity", complexActivity())
        );
    }

    @Test
    void shouldDeleteActivity() throws Exception {
        // given
        var activity = minimalActivity();

        // when
        mockMvc.perform(delete(ACTIVITY_URL + "/" + activity.id()))
                .andExpect(status().isOk());

        // then
        verify(activityApplicationService).deleteActivity(activity.id());
    }

    @Test
    void shouldUpdateActivityTitle() throws Exception {
        // given
        var activity = minimalActivity().withTitle("new title");
        var restfulActivity = aRestfulActivity(activity);
        when(activityApplicationService.renameActivity(activity.title(), activity.id()))
                .thenReturn(restfulActivity.asActivityResult());

        // when / then
        mockMvc.perform(put(ACTIVITY_URL + "/" + activity.id() + "/title")
                        .contentType(TEXT_PLAIN)
                        .content(activity.title()))
                .andExpect(status().isOk())
                .andExpect(content().json(restfulActivity.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    @Test
    void shouldUpdateActivityStartTime() throws Exception {
        // given
        var activity = minimalActivity().startedAt(Instant.ofEpochMilli(10L));
        var restfulActivity = aRestfulActivity(activity);
        when(activityApplicationService.startActivity(activity.startTime(), activity.id()))
                .thenReturn(restfulActivity.asActivityResult());

        // when / then
        mockMvc.perform(put(ACTIVITY_URL + "/" + activity.id() + "/startTime")
                        .contentType(APPLICATION_JSON)
                        .content(jsonValue(activity.startTime())))
                .andExpect(status().isOk())
                .andExpect(content().json(restfulActivity.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    @Test
    void shouldUpdateActivityEndTime() throws Exception {
        // given
        var activity = minimalActivity().endedAt(Instant.ofEpochMilli(20L));
        var restfulActivity = aRestfulActivity(activity);
        when(activityApplicationService.finishActivity(activity.endTime(), activity.id()))
                .thenReturn(restfulActivity.asActivityResult());

        // when / then
        mockMvc.perform(put(ACTIVITY_URL + "/" + activity.id() + "/endTime")
                        .contentType(APPLICATION_JSON)
                        .content(jsonValue(activity.endTime())))
                .andExpect(status().isOk())
                .andExpect(content().json(restfulActivity.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    @Test
    void shouldUpdateActivityComment() throws Exception {
        // given
        var activity = minimalActivity().withComment("new comment");
        var restfulActivity = aRestfulActivity(activity);
        when(activityApplicationService.updateActivityComment(activity.comment(), activity.id()))
                .thenReturn(restfulActivity.asActivityResult());

        // when / then
        mockMvc.perform(put(ACTIVITY_URL + "/" + activity.id() + "/comment")
                        .contentType(TEXT_PLAIN)
                        .content(activity.comment()))
                .andExpect(status().isOk())
                .andExpect(content().json(restfulActivity.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }
}
