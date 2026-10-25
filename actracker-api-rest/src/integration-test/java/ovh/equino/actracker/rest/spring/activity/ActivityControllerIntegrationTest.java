package ovh.equino.actracker.rest.spring.activity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.application.SortCriteria;
import ovh.equino.actracker.application.activity.ActivityApplicationService;
import ovh.equino.actracker.application.activity.ActivityResult;
import ovh.equino.actracker.application.activity.SearchActivitiesQuery;
import ovh.equino.actracker.domain.activity.ActivityTestData;
import ovh.equino.actracker.rest.spring.ControllerIntegrationTest;

import java.time.Instant;
import java.util.List;
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("searchActivitiesQueryCases")
    void shouldSearchActivitiesWithFilters(String testName,
                                           SearchActivitiesQuery expectedQuery,
                                           org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requestBuilder)
            throws Exception {
        // given
        when(activityApplicationService.searchActivities(any(SearchActivitiesQuery.class)))
                .thenReturn(new SearchResult<>(null, List.of()));

        // when
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());

        // then
        verify(activityApplicationService).searchActivities(expectedQuery);
    }

    private static Stream<Arguments> searchActivitiesQueryCases() {
        UUID tag1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID tag2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID excludedActivity = UUID.fromString("33333333-3333-3333-3333-333333333333");

        SortCriteria sortCriteria = new SortCriteria();
        sortCriteria.orderBy("startTime", "desc");

        return Stream.of(
                Arguments.of("no filters",
                        new SearchActivitiesQuery(null, null, null, null, null, Set.of(), Set.of(), new SortCriteria()),
                        get(ACTIVITY_URL + "/matching")),
                Arguments.of("pageId filter",
                        new SearchActivitiesQuery(null, "page-123", null, null, null, Set.of(), Set.of(), new SortCriteria()),
                        get(ACTIVITY_URL + "/matching").queryParam("pageId", "page-123")),
                Arguments.of("pageSize filter",
                        new SearchActivitiesQuery(25, null, null, null, null, Set.of(), Set.of(), new SortCriteria()),
                        get(ACTIVITY_URL + "/matching").queryParam("pageSize", "25")),
                Arguments.of("term filter",
                        new SearchActivitiesQuery(null, null, "gym", null, null, Set.of(), Set.of(), new SortCriteria()),
                        get(ACTIVITY_URL + "/matching").queryParam("term", "gym")),
                Arguments.of("requiredTags filter",
                        new SearchActivitiesQuery(null, null, null, null, null, Set.of(tag1, tag2), Set.of(), new SortCriteria()),
                        get(ACTIVITY_URL + "/matching").queryParam("requiredTags", tag1.toString() + "," + tag2.toString())),
                Arguments.of("excludedActivities filter",
                        new SearchActivitiesQuery(null, null, null, null, null, Set.of(), Set.of(excludedActivity), new SortCriteria()),
                        get(ACTIVITY_URL + "/matching").queryParam("excludedActivities", excludedActivity.toString())),
                Arguments.of("rangeStartMillis filter",
                        new SearchActivitiesQuery(null, null, null, Instant.ofEpochMilli(1000L), null, Set.of(), Set.of(), new SortCriteria()),
                        get(ACTIVITY_URL + "/matching").queryParam("rangeStartMillis", "1000")),
                Arguments.of("rangeEndMillis filter",
                        new SearchActivitiesQuery(null, null, null, null, Instant.ofEpochMilli(2000L), Set.of(), Set.of(), new SortCriteria()),
                        get(ACTIVITY_URL + "/matching").queryParam("rangeEndMillis", "2000")),
                Arguments.of("orderBy filter",
                        new SearchActivitiesQuery(null, null, null, null, null, Set.of(), Set.of(), sortCriteria),
                        get(ACTIVITY_URL + "/matching").queryParam("orderBy", "startTime.desc")),
                Arguments.of("all filters combined",
                        new SearchActivitiesQuery(10, "p1", "run", Instant.ofEpochMilli(500L), Instant.ofEpochMilli(1500L),
                                Set.of(tag1), Set.of(excludedActivity), sortCriteria),
                        get(ACTIVITY_URL + "/matching")
                                .queryParam("pageSize", "10")
                                .queryParam("pageId", "p1")
                                .queryParam("term", "run")
                                .queryParam("requiredTags", tag1.toString())
                                .queryParam("excludedActivities", excludedActivity.toString())
                                .queryParam("rangeStartMillis", "500")
                                .queryParam("rangeEndMillis", "1500")
                                .queryParam("orderBy", "startTime.desc"))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("searchActivitiesMappingCases")
    void shouldSearchActivitiesMapResults(String testName, SearchResult<ActivityResult> searchResult, String expectedResponse) throws Exception {
        // given
        when(activityApplicationService.searchActivities(any(SearchActivitiesQuery.class)))
                .thenReturn(searchResult);

        // when / then
        mockMvc.perform(get(ACTIVITY_URL + "/matching"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    private static Stream<Arguments> searchActivitiesMappingCases() {
        var emptyResult = new SearchResult<ActivityResult>(null, List.of());

        var minimalActivityData = minimalActivity();
        var minimalResult = aRestfulActivity(minimalActivityData).asActivityResult();
        var minimalResponseActivity = aRestfulActivity(minimalActivityData).asHttpResponse();

        var complex1 = complexActivity();
        var complex2 = complexActivity().withTitle(complexActivity().title() + "-2");
        var complex3 = complexActivity().withTitle(complexActivity().title() + "-3");
        var complexResult1 = aRestfulActivity(complex1).asActivityResult();
        var complexResult2 = aRestfulActivity(complex2).asActivityResult();
        var complexResult3 = aRestfulActivity(complex3).asActivityResult();
        var complexResponse1 = aRestfulActivity(complex1).asHttpResponse();
        var complexResponse2 = aRestfulActivity(complex2).asHttpResponse();
        var complexResponse3 = aRestfulActivity(complex3).asHttpResponse();

        return Stream.of(
                Arguments.of("empty result and empty nextPageId",
                        emptyResult,
                        """
                                {
                                    "nextPageId": null,
                                    "results": []
                                }
                                """),
                Arguments.of("single minimal activity and empty nextPageId",
                        new SearchResult<>(null, List.of(minimalResult)),
                        """
                                {
                                    "nextPageId": null,
                                    "results": [
                                        %s
                                    ]
                                }
                                """.formatted(minimalResponseActivity)),
                Arguments.of("three complex activities and non-empty nextPageId",
                        new SearchResult<>("next-page-id", List.of(complexResult1, complexResult2, complexResult3)),
                        """
                                {
                                    "nextPageId": "next-page-id",
                                    "results": [
                                        %s,
                                        %s,
                                        %s
                                    ]
                                }
                                """.formatted(complexResponse1, complexResponse2, complexResponse3))
        );
    }
}
