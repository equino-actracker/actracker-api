package ovh.equino.actracker.rest.spring.activity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.application.SortCriteria;
import ovh.equino.actracker.application.activity.ActivityApplicationService;
import ovh.equino.actracker.application.activity.ActivityResult;
import ovh.equino.actracker.application.activity.SearchActivitiesQuery;
import ovh.equino.actracker.domain.activity.ActivityTestData;
import ovh.equino.actracker.rest.spring.ControllerIntegrationTest;
import ovh.equino.actracker.rest.spring.SearchRestTestData;

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
    private static final String MATCHING_ACTIVITIES_URL = ACTIVITY_URL + "/matching";

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
                                                MockHttpServletRequestBuilder requestBuilder)
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

    private static Stream<Arguments> searchActivitiesCriteria() {
        var emptyPageId = (String) null;
        var emptyPageSize = (Integer) null;
        var emptyTerm = (String) null;
        var emptyRangeStart = (Instant) null;
        var emptyRangeEnd = (Instant) null;
        var emptyTags = Set.<UUID>of();
        var emptyExcludedActivities = Set.<UUID>of();
        var emptySortCriteria = new SortCriteria();

        var tag1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var tag2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        var excludedActivity = UUID.fromString("33333333-3333-3333-3333-333333333333");
        var sortCriteria = new SortCriteria();
        sortCriteria.orderBy("startTime", "desc");

        return Stream.of(
                Arguments.of("no filters",
                        new SearchActivitiesQuery(emptyPageSize, emptyPageId, emptyTerm, emptyRangeStart, emptyRangeEnd,
                                emptyTags, emptyExcludedActivities, emptySortCriteria),
                        get(MATCHING_ACTIVITIES_URL)),
                Arguments.of("pageId filter",
                        new SearchActivitiesQuery(emptyPageSize, "page-id", emptyTerm, emptyRangeStart, emptyRangeEnd,
                                emptyTags, emptyExcludedActivities, emptySortCriteria),
                        get(MATCHING_ACTIVITIES_URL).queryParam("pageId", "page-id")),
                Arguments.of("pageSize filter",
                        new SearchActivitiesQuery(25, emptyPageId, emptyTerm, emptyRangeStart, emptyRangeEnd,
                                emptyTags, emptyExcludedActivities, emptySortCriteria),
                        get(MATCHING_ACTIVITIES_URL).queryParam("pageSize", "25")),
                Arguments.of("term filter",
                        new SearchActivitiesQuery(emptyPageSize, emptyPageId, "gym", emptyRangeStart, emptyRangeEnd,
                                emptyTags, emptyExcludedActivities, emptySortCriteria),
                        get(MATCHING_ACTIVITIES_URL).queryParam("term", "gym")),
                Arguments.of("requiredTags filter",
                        new SearchActivitiesQuery(emptyPageSize, emptyPageId, emptyTerm, emptyRangeStart, emptyRangeEnd,
                                Set.of(tag1, tag2), emptyExcludedActivities, emptySortCriteria),
                        get(MATCHING_ACTIVITIES_URL).queryParam("requiredTags", tag1.toString() + "," + tag2.toString())),
                Arguments.of("excludedActivities filter",
                        new SearchActivitiesQuery(emptyPageSize, emptyPageId, emptyTerm, emptyRangeStart, emptyRangeEnd,
                                emptyTags, Set.of(excludedActivity), emptySortCriteria),
                        get(MATCHING_ACTIVITIES_URL).queryParam("excludedActivities", excludedActivity.toString())),
                Arguments.of("rangeStartMillis filter",
                        new SearchActivitiesQuery(emptyPageSize, emptyPageId, emptyTerm, Instant.ofEpochMilli(1000L), emptyRangeEnd,
                                emptyTags, emptyExcludedActivities, emptySortCriteria),
                        get(MATCHING_ACTIVITIES_URL).queryParam("rangeStartMillis", "1000")),
                Arguments.of("rangeEndMillis filter",
                        new SearchActivitiesQuery(emptyPageSize, emptyPageId, emptyTerm, emptyRangeStart, Instant.ofEpochMilli(2000L),
                                emptyTags, emptyExcludedActivities, emptySortCriteria),
                        get(MATCHING_ACTIVITIES_URL).queryParam("rangeEndMillis", "2000")),
                Arguments.of("orderBy filter",
                        new SearchActivitiesQuery(emptyPageSize, emptyPageId, emptyTerm, emptyRangeStart, emptyRangeEnd,
                                emptyTags, emptyExcludedActivities, sortCriteria),
                        get(MATCHING_ACTIVITIES_URL).queryParam("orderBy", "startTime.desc")),
                Arguments.of("all filters combined",
                        new SearchActivitiesQuery(10, "p1", "run", Instant.ofEpochMilli(500L), Instant.ofEpochMilli(1500L),
                                Set.of(tag1), Set.of(excludedActivity), sortCriteria),
                        get(MATCHING_ACTIVITIES_URL)
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
    @MethodSource("matchingActivities")
    void shouldFindMatchingActivities(String testName, SearchRestTestData<ActivityResult> expectedSearchResult) throws Exception {
        // given
        when(activityApplicationService.searchActivities(any(SearchActivitiesQuery.class)))
                .thenReturn(expectedSearchResult.asEntitySearchResult());

        // when / then
        mockMvc.perform(get(MATCHING_ACTIVITIES_URL))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedSearchResult.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    private static Stream<Arguments> matchingActivities() {
        var restfulMinimalActivity = aRestfulActivity(minimalActivity());
        var restfulComplexActivity1 = aRestfulActivity(complexActivity());
        var restfulComplexActivity2 = aRestfulActivity(complexActivity());
        var restfulComplexActivity3 = aRestfulActivity(complexActivity());

        return Stream.of(
                Arguments.of("no matching activities and no next page",
                        new SearchRestTestData<ActivityResult>(null, List.of())),
                Arguments.of("single minimal activity and no next page",
                        new SearchRestTestData<ActivityResult>(null, List.of(restfulMinimalActivity))),
                Arguments.of("three complex activities and next page",
                        new SearchRestTestData<ActivityResult>("next-page-id",
                                List.of(restfulComplexActivity1, restfulComplexActivity2, restfulComplexActivity3)))
        );
    }
}
