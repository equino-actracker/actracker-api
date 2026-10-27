package ovh.equino.actracker.rest.spring.activity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ovh.equino.actracker.application.SortCriteria;
import ovh.equino.actracker.application.activity.ActivityApplicationService;
import ovh.equino.actracker.application.activity.ActivityResult;
import ovh.equino.actracker.application.activity.SearchActivitiesQuery;
import ovh.equino.actracker.domain.activity.ActivityTestData;
import ovh.equino.actracker.rest.spring.ControllerIntegrationTest;
import ovh.equino.actracker.rest.spring.SearchRestTestData;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
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
                .thenReturn(restfulActivity.asEntityResult());

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
                .thenReturn(restfulActivity.asEntityResult());

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
                .thenReturn(restfulActivity.asEntityResult());

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
                .thenReturn(restfulActivity.asEntityResult());

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
                .thenReturn(restfulActivity.asEntityResult());

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
                .thenReturn(restfulActivity.asEntityResult());

        // when / then
        mockMvc.perform(put(ACTIVITY_URL + "/" + activity.id() + "/comment")
                        .contentType(TEXT_PLAIN)
                        .content(activity.comment()))
                .andExpect(status().isOk())
                .andExpect(content().json(restfulActivity.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("searchActivitiesCriteria")
    void shouldSearchActivitiesMatchingCriteria(String testName,
                                                SearchActivitiesQuery expectedQuery,
                                                Map<String, String> searchCriteria)
            throws Exception {

        // when
        var requestBuilder = get(ACTIVITY_URL + "/matching");
        searchCriteria.forEach((name, value) -> requestBuilder.queryParam(name, value));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());

        // then
        verify(activityApplicationService).searchActivities(expectedQuery);
    }

    private static Stream<Arguments> searchActivitiesCriteria() {
        return Stream.of(
                Arguments.of("empty criteria",
                        new SearchActivitiesQuery(null, null, null, null, null, null, null, null),
                        Map.of()),
                Arguments.of("pageSize filter",
                        new SearchActivitiesQuery(25, null, null, null, null, null, null, null),
                        Map.of("pageSize", "25")),
                Arguments.of("pageId filter",
                        new SearchActivitiesQuery(null, "page-id", null, null, null, null, null, null),
                        Map.of("pageId", "page-id")),
                Arguments.of("term filter",
                        new SearchActivitiesQuery(null, null, "gym", null, null, null, null, null),
                        Map.of("term", "gym")),
                Arguments.of("rangeStartMillis filter",
                        new SearchActivitiesQuery(null, null, null, Instant.ofEpochMilli(1000L), null, null, null, null),
                        Map.of("rangeStartMillis", "1000")),
                Arguments.of("rangeEndMillis filter",
                        new SearchActivitiesQuery(null, null, null, null, Instant.ofEpochMilli(2000L), null, null, null),
                        Map.of("rangeEndMillis", "2000")),
                Arguments.of("requiredTags filter",
                        new SearchActivitiesQuery(null, null, null, null, null,
                                Set.of(new UUID(1L, 1L), new UUID(2L, 2L)), null, null),
                        Map.of("requiredTags", new UUID(1L, 1L) + "," + new UUID(2L, 2L))),
                Arguments.of("excludedActivities filter",
                        new SearchActivitiesQuery(null, null, null, null, null, null,
                                Set.of(new UUID(3L, 3L)), null),
                        Map.of("excludedActivities", new UUID(3L, 3L).toString())),
                Arguments.of("orderBy filter",
                        new SearchActivitiesQuery(null, null, null, null, null, null, null,
                                new SortCriteria().orderBy("startTime", "desc")),
                        Map.of("orderBy", "startTime.desc")),
                Arguments.of("all filters combined",
                        new SearchActivitiesQuery(10, "p1", "run", Instant.ofEpochMilli(500L), Instant.ofEpochMilli(1500L),
                                Set.of(new UUID(4L, 4L)), Set.of(new UUID(5L, 5L)),
                                new SortCriteria().orderBy("startTime", "desc")),
                        Map.of("pageSize", "10",
                                "pageId", "p1",
                                "term", "run",
                                "rangeStartMillis", "500",
                                "rangeEndMillis", "1500",
                                "requiredTags", new UUID(4L, 4L).toString(),
                                "excludedActivities", new UUID(5L, 5L).toString(),
                                "orderBy", "startTime.desc"))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("matchingActivities")
    void shouldFindMatchingActivities(String testName, SearchRestTestData<ActivityResult> expectedSearchResult) throws Exception {
        // given
        when(activityApplicationService.searchActivities(any(SearchActivitiesQuery.class)))
                .thenReturn(expectedSearchResult.asEntitySearchResult());

        // when / then
        mockMvc.perform(get(ACTIVITY_URL + "/matching"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedSearchResult.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    private static Stream<Arguments> matchingActivities() {
        return Stream.of(
                Arguments.of("no matching activities and no next page",
                        new SearchRestTestData<ActivityResult>(null, List.of())),
                Arguments.of("single minimal activity and no next page",
                        new SearchRestTestData<ActivityResult>(null, List.of(aRestfulActivity(minimalActivity())))),
                Arguments.of("three complex activities and next page",
                        new SearchRestTestData<ActivityResult>("next-page-id", List.of(
                                aRestfulActivity(complexActivity()),
                                aRestfulActivity(complexActivity()),
                                aRestfulActivity(complexActivity()))))
        );
    }
}
